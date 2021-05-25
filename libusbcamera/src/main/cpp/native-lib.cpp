#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <android/log.h>
#include <opencv2/video/tracking.hpp>
#include <GLES2/gl2.h>

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define  LOG_TAG    "JNI_LOG"
#define  ALOG(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#include <time.h> // clock_gettime

using namespace std;
using namespace cv;

struct Trajectory {
    Trajectory() {}

    Trajectory(double _x, double _y, double _a) {
        x = _x;
        y = _y;
        a = _a;
    }

    // "+"
    friend Trajectory operator+(const Trajectory &c1, const Trajectory &c2) {
        return Trajectory(c1.x + c2.x, c1.y + c2.y, c1.a + c2.a);
    }

    //"-"
    friend Trajectory operator-(const Trajectory &c1, const Trajectory &c2) {
        return Trajectory(c1.x - c2.x, c1.y - c2.y, c1.a - c2.a);
    }

    //"*"
    friend Trajectory operator*(const Trajectory &c1, const Trajectory &c2) {
        return Trajectory(c1.x * c2.x, c1.y * c2.y, c1.a * c2.a);
    }

    //"/"
    friend Trajectory operator/(const Trajectory &c1, const Trajectory &c2) {
        return Trajectory(c1.x / c2.x, c1.y / c2.y, c1.a / c2.a);
    }

    //"="
    Trajectory operator=(const Trajectory &rx) {
        x = rx.x;
        y = rx.y;
        a = rx.a;
        return Trajectory(x, y, a);
    }

    double x;
    double y;
    double a; // angle
};

struct TransformParam {
    TransformParam() {}

    TransformParam(double _dx, double _dy, double _da) {
        dx = _dx;
        dy = _dy;
        da = _da;
    }

    double dx;
    double dy;
    double da; // angle
};

//static Tracker tracker;
static uint8_t lastMode = 0;
static Mat prevm;
static Mat prev_grey;
static Mat last_T;
double a = 0;
double x = 0;
double y = 0;
// Step 2 - Accumulate the transformations to get the image trajectory
vector<Trajectory> trajectory; // trajectory at all frames
//
// Step 3 - Smooth out the trajectory using an averaging window
vector<Trajectory> smoothed_trajectory; // trajectory at all frames
Trajectory X;//posteriori state estimate
Trajectory X_;//priori estimate
Trajectory P;// posteriori estimate error covariance
Trajectory P_;// priori estimate error covariance
Trajectory K;//gain
Trajectory z;//actual measurement
double pstd = 4e-3;//can be changed
double cstd = 0.25;//can be changed
Trajectory Q(pstd, pstd, pstd);// process noise covariance
Trajectory R(cstd, cstd, cstd);// measurement noise covariance
vector<TransformParam> new_prev_to_cur_transform;
int isUpdateTrajectory = 1;

int rectWidth = 612;
int rectHeight = 612;
//uint8_t isUpdatePrevImg=0;
extern "C"
JNIEXPORT void JNICALL
Java_com_cnsj_sightaid_MyGLSurfaceView_processFrame(JNIEnv *env, jclass clazz, jint tex1,
                                                    jint tex2, jint w, jint h,
                                                    jboolean isstab, jint edgemode) {

    // TODO: implement processFrame()
    static UMat m;
    uint8_t isUpdatePrevImg = 0;
    UMat mCrop;
    Mat frame, orig, orig_warped, tmp;


    static UMat m2;
    int64_t t;
    m.create(h, w, CV_8UC4);
    mCrop.create(h, w, CV_8UC4);

    m2.create(h, w, CV_8UC4);
    // read
    // expecting FBO to be bound, read pixels to mat

//#if   1
    if (edgemode == 0) {
        glReadPixels(0, 0, m.cols, m.rows, GL_RGBA, GL_UNSIGNED_BYTE, m.getMat(ACCESS_WRITE).data);
//        LOGD("colsrows: %d %d",m.cols,m.rows);
//#else
    } else if (edgemode == 1) {
        glReadPixels(0, 0, m2.cols, m2.rows, GL_RGBA, GL_UNSIGNED_BYTE,
                     m2.getMat(ACCESS_WRITE).data);
        GaussianBlur(m2, m, Size(3, 3), 5.0f, 5.0f);
        cvtColor(m, m, COLOR_BGRA2GRAY);
        Laplacian(m, m, CV_8U);
        multiply(m, 10, m);
        cvtColor(m, m, COLOR_GRAY2BGRA);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tex2);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, m.cols, m.rows, GL_RGBA, GL_UNSIGNED_BYTE,
                        m.getMat(ACCESS_READ).data);
        return;
    } else if (edgemode == 2) {
        glReadPixels(0, 0, m2.cols, m2.rows, GL_RGBA, GL_UNSIGNED_BYTE,
                     m2.getMat(ACCESS_WRITE).data);
        GaussianBlur(m2, m, Size(3, 3), 5.0f, 5.0f);
        cvtColor(m, m, COLOR_BGRA2GRAY);
        Laplacian(m, m, CV_8U);
        multiply(m, 10, m);
        bitwise_not(m, m);
        cvtColor(m, m, COLOR_GRAY2BGRA);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tex2);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, m.cols, m.rows, GL_RGBA, GL_UNSIGNED_BYTE,
                        m.getMat(ACCESS_READ).data);
        return;
    }
//#endif

    if (isstab) {
        if (lastMode == 0) {
            isUpdatePrevImg = 1;
        }
        lastMode = 1;
        orig = m.getMat(ACCESS_RW);
        orig = orig(Rect((w / 2 - rectWidth / 2), (h / 2 - rectHeight / 2), rectWidth, rectHeight));
//        if(isUpdatePrevImg==1){
//            LOGD("Processing on CPU");
//            isUpdatePrevImg=0;
//            tracker.updatePrevImage(orig);
//        }

        Mat orig_gray;
        cvtColor(orig, orig_gray, COLOR_BGR2GRAY);

        vector<Point2f> prev_corner, cur_corner;
        vector<Point2f> prev_corner2, cur_corner2;
        vector<uchar> status;
        vector<float> err;

        if (isUpdatePrevImg == 1) {//开始防抖处理
            cvtColor(orig, prev_grey, COLOR_BGR2GRAY);
            prevm = orig.clone();
            a = 0;
            x = 0;
            y = 0;
        }

        goodFeaturesToTrack(prev_grey, prev_corner, 50, 0.01, 30);

        if (prev_corner.size() != 0) {
            calcOpticalFlowPyrLK(prev_grey, orig_gray, prev_corner, cur_corner, status, err);
            for (size_t i = 0; i < status.size(); i++) {
                if (status[i]) {
                    prev_corner2.push_back(prev_corner[i]);
                    cur_corner2.push_back(cur_corner[i]);
                }
            }
        } else {
            goto safeexit;
        }
        // weed out bad matches


        // translation + rotation only
//        LOGD("glTexSubImage2Dsize()2 costs %d", cur_corner2.size());
        Mat T;
        if (cur_corner2.size() >= 5) {
            T = estimateRigidTransform(prev_corner2, cur_corner2,
                                       false); // false = rigid transform, no scaling/shearing
        } else {
            last_T.copyTo(T);
        }
//        Mat T = estimateRigidTransform(prev_corner2, cur_corner2, false); // false = rigid transform, no scaling/shearing
        if (T.data == NULL) {
            last_T.copyTo(T);
        }

        T.copyTo(last_T);

        // decompose T
        double dx = T.at<double>(0, 2);
        double dy = T.at<double>(1, 2);
        double da = atan2(T.at<double>(1, 0), T.at<double>(0, 0));

        x += dx;
        y += dy;
        a += da;

        z = Trajectory(x, y, a);

        if (isUpdatePrevImg == 1) {
            isUpdateTrajectory = 0;
            // intial guesses
            X = Trajectory(0, 0, 0); //Initial estimate,  set 0
            P = Trajectory(1, 1, 1); //set error variance,set 1
        } else {
            //time update£¨prediction£©
            X_ = X; //X_(k) = X(k-1);
            P_ = P + Q; //P_(k) = P(k-1)+Q;
            // measurement update£¨correction£©
            K = P_ / (P_ + R); //gain;K(k) = P_(k)/( P_(k)+R );
            X = X_ + K * (z - X_); //z-X_ is residual,X(k) = X_(k)+K(k)*(z(k)-X_(k));
            P = (Trajectory(1, 1, 1) - K) * P_; //P(k) = (1-K(k))*P_(k);
        }
        // target - current
        double diff_x = X.x - x;//
        double diff_y = X.y - y;
        double diff_a = X.a - a;

        dx = dx + diff_x;
        dy = dy + diff_y;
        da = da + diff_a;
        if (abs(da) > 1.0 || abs(dx) > 20 || abs(dy) > 20) {
            dx = 0.0;
            dy = 0.0;
            da = 0.0;
        }
        T.at<double>(0, 0) = cos(da);
        T.at<double>(0, 1) = -sin(da);
        T.at<double>(1, 0) = sin(da);
        T.at<double>(1, 1) = cos(da);

        T.at<double>(0, 2) = dx;
        T.at<double>(1, 2) = dy;

        warpAffine(prevm, mCrop, T, Size());
        prevm = orig.clone();
        orig_gray.copyTo(prev_grey);


//        tracker.processImage(orig);
//        Mat invTrans = tracker.rigidTransform.inv(DECOMP_SVD);
        //m.copyTo(mCrop);
//        warpAffine(m, mCrop, invTrans.rowRange(0, 2), Size());
        Point2f center;
        center.x = 780;
        center.y = 360;
//        LOGD("center position %d %d", center.x,center.y);
        circle(mCrop, center, 10, Scalar(0, 255, 255), FILLED);
        // write back
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tex2);
        glTexSubImage2D(GL_TEXTURE_2D, 0, (w / 2 - rectWidth / 2), (h / 2 - rectHeight / 2),
                        mCrop.cols, mCrop.rows, GL_RGBA, GL_UNSIGNED_BYTE,
                        mCrop.getMat(ACCESS_READ).data);
    } else {

        safeexit:
        lastMode = 0;
        // write back
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tex2);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, m.cols, m.rows, GL_RGBA, GL_UNSIGNED_BYTE,
                        m.getMat(ACCESS_READ).data);
    }

}



extern "C"
JNIEXPORT void JNICALL
Java_com_serenegiant_usb_widget_OpenCVView_resetFrame(JNIEnv *env, jobject thiz) {
    lastMode = 0;

}
extern "C"
JNIEXPORT void JNICALL
Java_com_serenegiant_usb_widget_OpenCVView_processFrame(JNIEnv *env, jobject thiz,
                                                        jint tex2, jint w, jint h, jboolean isstab,
                                                        jint edgemode) {
    static UMat m;
    UMat mCrop;
    uint8_t isUpdatePrevImg = 0;
    Mat frame, orig, orig_tarped, tmp;
    m.create(h, w, CV_8UC4);
    mCrop.create(h, w, CV_8UC4);
    glReadPixels(0, 0, m.cols, m.rows, GL_RGBA, GL_UNSIGNED_BYTE, m.getMat(ACCESS_WRITE).data);
    if (isstab) {
        if (lastMode == 0) {
            isUpdatePrevImg = 1;
        }
        lastMode = 1;
        orig = m.getMat(ACCESS_RW);
        orig = orig(Rect((w / 2 - rectWidth / 2), (h / 2 - rectHeight / 2), rectWidth, rectHeight));
        Mat orig_gray;
        //转灰度图像
        cvtColor(orig, orig_gray, COLOR_BGR2GRAY);
        vector<Point2f> prev_corner, cur_corner;
        vector<Point2f> prev_corner2, cur_corner2;
        vector<uchar> status;
        vector<float> err;
        if (isUpdatePrevImg == 1) {//开始防抖处理
            cvtColor(orig, prev_grey, COLOR_BGR2GRAY);
            prevm = orig.clone();
            a = 0;
            x = 0;
            y = 0;
        }
        //对灰度图像进行角点检测（物体特征点检测） false 使用Shi Tomasi 算法，true 使用Harris 算法 Harris算法较慢
        goodFeaturesToTrack(prev_grey, prev_corner, 50, 0.01, 30);
        if (prev_corner.size() != 0) {
            //使用具有金字塔的迭代Lucas-Kanade方法计算稀疏特征集的光流
            calcOpticalFlowPyrLK(prev_grey, orig_gray, prev_corner, cur_corner, status, err);
            for (size_t i = 0; i < status.size(); i++) {
                if (status[i]) {
                    prev_corner2.push_back(prev_corner[i]);
                    cur_corner2.push_back(cur_corner[i]);
                }
            }
        } else {
            goto safeexit;
        }
        Mat T;
        if (cur_corner2.size() >= 5) {
            //求取仿射变换
            T = estimateRigidTransform(prev_corner2, cur_corner2,
                                       false); // false = rigid transform, no scaling/shearing
        } else {
            last_T.copyTo(T);
        }
        if (T.data == NULL) {
            last_T.copyTo(T);
        }

        T.copyTo(last_T);
        // decompose T
        double dx = T.at<double>(0,2);
        double dy = T.at<double>(1,2);
        double da = atan2(T.at<double>(1,0), T.at<double>(0,0));
        x += dx;
        y += dy;
        a += da;
        z = Trajectory(x,y,a);
        if(isUpdatePrevImg==1){
            isUpdateTrajectory=0;
            // intial guesses
            X = Trajectory(0,0,0); //Initial estimate,  set 0
            P =Trajectory(1,1,1); //set error variance,set 1
        }else{
            //time update£¨prediction£©
            X_ = X; //X_(k) = X(k-1);
            P_ = P+Q; //P_(k) = P(k-1)+Q;
            // measurement update£¨correction£©
            K = P_/( P_+R ); //gain;K(k) = P_(k)/( P_(k)+R );
            X = X_+K*(z-X_); //z-X_ is residual,X(k) = X_(k)+K(k)*(z(k)-X_(k));
            P = (Trajectory(1,1,1)-K)*P_; //P(k) = (1-K(k))*P_(k);
        }
        // target - current
        double diff_x = X.x - x;//
        double diff_y = X.y - y;
        double diff_a = X.a - a;
        dx = dx + diff_x;
        dy = dy + diff_y;
        da = da + diff_a;
        if(abs(da)>1.0||abs(dx)>20||abs(dy)>20){
            dx=0.0;
            dy=0.0;
            da=0.0;
        }
        T.at<double>(0,0) = cos(da);
        T.at<double>(0,1) = -sin(da);
        T.at<double>(1,0) = sin(da);
        T.at<double>(1,1) = cos(da);
        T.at<double>(0,2) = dx;
        T.at<double>(1,2) = dy;
        warpAffine(prevm, mCrop, T, Size());
        prevm=orig.clone();
        orig_gray.copyTo(prev_grey);
        Point2f center;
        center.x=780;
        center.y=360;
        circle(mCrop,center,10,Scalar(0,255,255),FILLED);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tex2);
        glTexSubImage2D(GL_TEXTURE_2D, 0, (w/2-rectWidth/2), (h/2-rectHeight/2), mCrop.cols, mCrop.rows, GL_RGBA, GL_UNSIGNED_BYTE, mCrop.getMat(ACCESS_READ).data);
    } else {
        safeexit:
        lastMode = 0;
        // write back
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, tex2);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, m.cols, m.rows, GL_RGBA, GL_UNSIGNED_BYTE,
                        m.getMat(ACCESS_READ).data);
    }
}