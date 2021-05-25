package com.serenegiant.usb.widget;

public class shadercode {
    //=====================================ORIGIN CODE
    public static final String OriginVSS =
            "#version 100\n" +
                    "uniform mat4 uMVPMatrix;\n" +
//                    "uniform mat4 uTexMatrix;\n" +
//                    "uniform mat4 uQuatMatrix;\n" +
                    "attribute highp vec4 aPosition;\n" +
                    "attribute highp vec4 aTextureCoord;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = uMVPMatrix * aPosition;\n" +
//                    "    vTextureCoord = (uQuatMatrix * aTextureCoord).xy;\n" +
                    "    vTextureCoord = aTextureCoord.xy;\n" +
                    "}\n";

    public static final String OriginFSS1 = "#version 100\n" +
            "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "varying highp vec2 vTextureCoord;\n" +
            "void main() {\n  " +
            "gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}";

    public static final String OriginFSS11 =
            "#version 100\n" +
                    "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
//                    "uniform sampler2D sTexture;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "uniform int mUserMode;\n" +
                    "uniform float mSaturation;\n" +
                    "uniform float mContrast;\n" +
                    "uniform float mBrightness;\n" +
                    "uniform vec2 piexlSize;\n" +
                    "uniform float aryWeight[9];\n" +
                    "uniform vec2 aryOffset[9];\n" +//给出卷积内核中各个元素对应像素相对于待处理像素的纹理坐标偏移量
                    "uniform float scaleFactor;\n" +//给出最终求和时的加权因子(为调整亮度)
                    "uniform float offsetBase;\n" +////给出卷积内核中各个元素对应像素相对于待处理像素的纹理坐标偏移量 除数
                    "void make_kernal(inout vec4 n[9]){\n" +
                    "float w = 5.0 / piexlSize.x;\nfloat h = 5.0 / piexlSize.y;\n" +
                    "vec3 m = texture2D(sTexture, vTextureCoord + vec2( -w, -h)).rgb;\n" +
                    "n[0] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
                    "m = texture2D(sTexture, vTextureCoord + vec2(0.0, -h)).rgb;\n" +
                    "n[1] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
                    "m = texture2D(sTexture, vTextureCoord + vec2(  w, -h)).rgb;\n" +
                    "n[2] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
                    "m = texture2D(sTexture, vTextureCoord + vec2( -w, 0.0)).rgb;\n" +
                    "n[3] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
                    "m = texture2D(sTexture, vTextureCoord).rgb;\n" +
                    "n[4] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
                    "m = texture2D(sTexture, vTextureCoord + vec2(  w, 0.0)).rgb;\n" +
                    "n[5] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
                    "m = texture2D(sTexture, vTextureCoord + vec2( -w, h)).rgb;\n" +
                    "n[6] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
                    "m = texture2D(sTexture, vTextureCoord + vec2(0.0, h)).rgb;\n" +
                    "n[7] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
                    "m = texture2D(sTexture, vTextureCoord + vec2(  w, h)).rgb;\n" +
                    "n[8] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
                    "}\n" +

                    "vec4 tmp(vec3 colorrbg){\n" +
                    "float PI = 3.1415926;\n" +
                    "float B = mBrightness;\n" +
                    "float c = mContrast;\n" +
                    "if(mUserMode==5||mUserMode==6){\n" +
                    "c=0.6*c+0.4;\n" +
                    "}\n" +
                    "float k = tan((45.0 + 44.0 * c) / 180.0 * PI);\n" +
                    "colorrbg = ((colorrbg*255.0 - 127.5 * (1.0 - B)) * k + 127.5 * (1.0 + B)) / 255.0;\n" +
                    "return vec4(colorrbg,1.0);\n " +
                    "}\n" +

                    "vec4 make_color(){\n" +
                    "vec4 sum;" +
//                    "if(vTextureCoord.s<0.495){\n" +
                    "for(int i=0;i<9;i++){\n" +
                    "sum+=texture2D(sTexture,vTextureCoord.st+aryOffset[i].xy/offsetBase)*aryWeight[i];" +
                    "};\n" +
//                    "}else if(vTextureCoord.s>0.505){\n" +
//                    "sum=texture2D(sTexture,vTextureCoord.st);\n" +
//                    "}else{\n" +
//                    "sum=vec4(1.0,0.0,0.0,1.0);\n" +
//                    "}\n" +
                    "return sum*scaleFactor;\n" +
//                    "vec4 n[9];\n" +
//                    "vec2 offset0=vec2(-1.0,-1.0); vec2 offset1=vec2(0.0,-1.0); vec2 offset2=vec2(1.0,-1.0);\n" +
//                    "vec2 offset3=vec2(-1.0,0.0); vec2 offset4=vec2(0.0,0.0); vec2 offset5=vec2(1.0,0.0);\n" +
//                    "vec2 offset6=vec2(-1.0,1.0); vec2 offset7=vec2(0.0,1.0); vec2 offset8=vec2(1.0,1.0);\n" +
//                    "const float scaleFactor = 1.0;\n" +//给出最终求和时的加权因子(为调整亮度)
////                    "//卷积内核中各个位置的值\n" +
////                    //Laplacian过滤
////                    "float kernelValue0 = 0.0; float kernelValue1 = 1.0; float kernelValue2 = 0.0;\n" +
////                    "float kernelValue3 = 1.0; float kernelValue4 = -4.0; float kernelValue5 = 1.0;\n" +
////                    "float kernelValue6 = 0.0; float kernelValue7 = 1.0; float kernelValue8 = 0.0;\n" +
//                    "float kernelValue0 = 0.0; float kernelValue1 = -1.0; float kernelValue2 = 0.0;\n" +
//                    "float kernelValue3 = -1.0; float kernelValue4 = 4.0; float kernelValue5 = -1.0;\n" +
//                    "float kernelValue6 = 0.0; float kernelValue7 = -1.0; float kernelValue8 = 0.0;\n" +
////                    "//获取卷积内核中各个元素对应像素的颜色值\n" +
//                    "n[0]=texture2D(sTexture, vTextureCoord.st + aryOffset[0].xy/512.0);\n" +
//                    "n[1]=texture2D(sTexture, vTextureCoord.st + aryOffset[1].xy/512.0);\n" +
//                    "n[2]=texture2D(sTexture, vTextureCoord.st + aryOffset[2].xy/512.0);\n" +
//                    "n[3]=texture2D(sTexture, vTextureCoord.st + aryOffset[3].xy/512.0);\n" +
//                    "n[4]=texture2D(sTexture, vTextureCoord.st + aryOffset[4].xy/512.0);\n" +
//                    "n[5]=texture2D(sTexture, vTextureCoord.st + aryOffset[5].xy/512.0);\n" +
//                    "n[6]=texture2D(sTexture, vTextureCoord.st + aryOffset[6].xy/512.0);\n" +
//                    "n[7]=texture2D(sTexture, vTextureCoord.st + aryOffset[7].xy/512.0);\n" +
//                    "n[8]=texture2D(sTexture, vTextureCoord.st + aryOffset[8].xy/512.0);\n" +
////                    "//颜色求和\n" +
//                    "vec4 sum =kernelValue0*n[0]+kernelValue1*n[1]+kernelValue2*n[2]+\n" +
//                    "kernelValue3*n[3]+kernelValue4*n[4]+kernelValue5*n[5]+\n" +
//                    "kernelValue6*n[6]+kernelValue7*n[7]+kernelValue8*n[8]; \n" +
//                    "return sum*scaleFactor;\n" +
                    "}\n" +

                    "void main() {\n" +
                    "  float y,u,v,r,g,b;\n" +
                    "  vec3 orgb = texture2D(sTexture, vTextureCoord).rgb;\n" +
                    "  r = orgb.r;\n" +
                    "  g = orgb.g;\n" +
                    "  b = orgb.b;\n" +
                    "  y = 0.299*r+0.587*g+0.114*b;\n" +
                    "  u = 0.564*(b - y);\n" +
                    "  v = 0.713*(r-y);\n " +
                    "  if(mUserMode == 0){\n" +
                    "       u = u * mSaturation;\n" +
                    "       v = v * mSaturation;\n" +
                    "       r = (y + 1.403 * v);\n" +
                    "       g = (y - 0.344 * u - 0.714 * v);\n" +
                    "       b = (y + 1.770 * u);" +
                    "  }else if(mUserMode == 1){\n" +
                    "vec4 sum=make_color();\n" +
                    "r = 1.0-sum.r;\n" +
                    "g = 1.0-sum.g;\n" +
                    "b = 1.0-sum.b;\n" +
                    "  }else if(mUserMode == 2){\n" +
                    "vec4 sum=make_color();\n" +
                    "r = sum.r;\n" +
                    "g = sum.g;\n" +
                    "b = sum.b;\n" +
                    "  }else if(mUserMode == 3){\n" +
                    "vec4 sum=make_color();\n" +
                    "r = 1.0-sum.r;\n" +
                    "g = 1.0-sum.g;\n" +
                    "b = sum.b;\n" +
                    "  }else if(mUserMode == 4){\n" +
                    "vec4 sum=make_color();\n" +
                    "r = sum.r;\n" +
                    "g = sum.g;\n" +
                    "b = 1.0-sum.b;\n" +
                    "  }else if(mUserMode == 5){\n" +
                    "r = y;\n" +
                    "g = y;\n" +
                    "b = y;\n" +
                    "}else if(mUserMode == 6){\n" +
                    "r = 1.0-y;\n" +
                    "g = 1.0-y;\n" +
                    "b = 1.0-y;\n" +
                    "}else if(mUserMode == 11){\n" +
                    "u = u*mSaturation;\n" +
                    "v = v*mSaturation;\n" +
                    "r = (y + 1.403 * v);\n" +
                    "g = (y - 0.344 * u - 0.714 * v);\n" +
                    "b = (y + 1.770 * u);\n" +
                    "vec3 color = vec3(r, g, b);\n" +
                    "vec3 colors[3];\n" +
                    "colors[0] = vec3(0.,0.,1.);\n" +
                    "colors[1] = vec3(1.,1.,0.);\n" +
                    "colors[2] = vec3(1.,0.,0.);\n" +
                    "float lum = (color.r + color.g + color.b)/3.;\n" +
                    "int idx = int(step(0.5,lum));\n" +
                    "vec3 rgb = mix(colors[idx],colors[idx+1],(lum-float(idx)*0.5)/0.5);\n" +
                    "r = rgb.r;\n" +
                    "g = rgb.g;\n" +
                    "b = rgb.b;\n" +
                    "}else if(mUserMode == 7){\n" +
                    "vec4 n[9];\n" +
                    "make_kernal(n);\n" +
                    "vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);\n" +
                    "vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);\n" +
                    "vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));\n" +
                    "r = 1.0-sobel.r;\n" +
                    "g = 1.0-sobel.g;\n" +
                    "b = 1.0-sobel.b;\n" +
                    "}else if(mUserMode == 8){\n" +
                    "vec4 n[9];\n" +
                    "make_kernal( n);\n" +
                    "vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);\n" +
                    "vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);\n" +
                    "vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));\n" +
                    "r = sobel.r;\n" +
                    "g = sobel.g;\n" +
                    "b = sobel.b;\n" +
                    "}else if(mUserMode == 9){\n" +
                    "vec4 n[9];\n" +
                    "make_kernal(n);\n" +
                    "vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);\n" +
                    "vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);\n" +
                    "vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));\n" +
                    "r = 1.0-sobel.r;\n" +
                    "g = 1.0-sobel.g;\n" +
                    "b = sobel.b;\n" +
                    "}else if(mUserMode == 10){\n" +
                    "vec4 n[9];\n" +
                    "make_kernal(n);\n" +
                    "vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);\n" +
                    "vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);\n" +
                    "vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));\n" +
                    "r = sobel.r;\n" +
                    "g = sobel.g;\n" +
                    "b = 1.0-sobel.b;\n" +
                    "}\n" +
                    "vec3 nrgb = vec3(r,g,b);\n" +
//                    "  gl_FragColor = vec4(r,g,b, 1.0);\n" +
                    "  gl_FragColor = tmp(nrgb);\n" +
                    "}";

    public static final String OriginFSS2 =
            "#version 100\n" +
                    "precision mediump float;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "varying highp vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  float y,u,v,r,g,b;\n" +
                    "  vec3 orgb = texture(sTexture, vTextureCoord).rgb;\n" +
                    "r = orgb.r;\n" +
                    "g = orgb.g;\n" +
                    "b = orgb.b;\n" +
                    "y = 0.299*r+0.587*g+0.114*b;\n" +
                    "u = 0.564*(b - y);\n" +
                    "v = 0.713*(r-y);\n" +
                    "r = y;\n" +
                    "g = y;\n" +
                    "b = y;\n" +
                    "gl_FragColor = vec4(r,g,b, 1.0);\n" +
//                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}";
}
