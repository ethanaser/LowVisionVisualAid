package com.cnsj.neptunglasses.view.gl;

public class YUVShaderCode {
    //顶点着色器
    public static final String VERTEXCODE = "#version 100\n" +
            "varying vec2 v_texcoord;\n" +
            "attribute vec4 position;\n" +
            "attribute vec2 texcoord;\n" +
            "uniform mat4 MVP;\n" +
            "void main() {\n" +
            "    v_texcoord = texcoord;\n" +
            "    gl_Position = MVP * position;\n" +
            "}";
    //顶点着色器
    public static final String VERTEXCODE1 = "#version 100\n" +
            "varying vec2 v_texcoord;\n" +
            "attribute vec4 position;\n" +
            "attribute vec4 texcoord;\n" +
            "uniform mat4 MVP;\n" +
            "uniform mat4 quatMatrix;\n" +
            "void main() {\n" +
            "    v_texcoord =  texcoord.xy;\n" +
            "    gl_Position =quatMatrix * MVP * position;\n" +
            "}";
    //片段着色器
    public static final String FRAGMENTCODE_YUV = "#version 100\n" +
            "precision highp float;\n" +
            "varying vec2 v_texcoord;\n" +
            "uniform lowp sampler2D s_textureY;\n" +
            "uniform lowp sampler2D s_textureU;\n" +
            "uniform lowp sampler2D s_textureV;\n" +
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
            "   float w = 5.0 / piexlSize.x;\nfloat h = 5.0 / piexlSize.y;\n" +
            "   vec3 m = texture2D(s_textureY, v_texcoord + vec2( -w, -h)).rgb;\n" +
            "   n[0] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_textureY, v_texcoord + vec2(0.0, -h)).rgb;\n" +
            "   n[1] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_textureY, v_texcoord + vec2(  w, -h)).rgb;\n" +
            "   n[2] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_textureY, v_texcoord + vec2( -w, 0.0)).rgb;\n" +
            "   n[3] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_textureY, v_texcoord).rgb;\n" +
            "   n[4] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_textureY, v_texcoord + vec2(  w, 0.0)).rgb;\n" +
            "   n[5] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_textureY, v_texcoord + vec2( -w, h)).rgb;\n" +
            "   n[6] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_textureY, v_texcoord + vec2(0.0, h)).rgb;\n" +
            "   n[7] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_textureY, v_texcoord + vec2(  w, h)).rgb;\n" +
            "   n[8] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "}\n" +

            "vec4 tmp(vec3 colorrbg){\n" +
            "   float PI = 3.1415926;\n" +
            "   float B = mBrightness;\n" +
            "   float c = mContrast;\n" +
            "   if(mUserMode==5||mUserMode==6){\n" +
            "       c=0.6*c+0.4;\n" +
            "   }\n" +
            "   float k = tan((45.0 + 44.0 * c) / 180.0 * PI);\n" +
            "   colorrbg = ((colorrbg*255.0 - 127.5 * (1.0 - B)) * k + 127.5 * (1.0 + B)) / 255.0;\n" +
            "   return vec4(colorrbg,1.0);\n " +
            "}\n" +

            "vec4 make_color(){\n" +
            "   vec4 sum;" +
            "   for(int i=0;i<9;i++){\n" +
            "       sum+=texture2D(s_textureY,v_texcoord.st+aryOffset[i].xy/offsetBase)*aryWeight[i];" +
            "   };\n" +
            "   return sum*scaleFactor;\n" +
            "}\n" +
            "void main() {\n" +
            "    float y, u, v, r, g, b;\n" +
            "    y = texture2D(s_textureY, v_texcoord).r;\n" +
            "    u = texture2D(s_textureU, v_texcoord).r;\n" +
            "    v = texture2D(s_textureV, v_texcoord).r;\n" +
            "    u = u -0.5;\n" +
            "    v = v -0.5;\n" +
            "    if(mUserMode == 0){\n" +
            "       u = u * mSaturation;\n" +
            "       v = v * mSaturation;\n" +
            "       r = (y + 1.403 * v);\n" +
            "       g = (y - 0.344 * u - 0.714 * v);\n" +
            "       b = (y + 1.770 * u);" +
            "    }else if(mUserMode == 1){\n" +
            "       vec4 sum=make_color();\n" +
            "       r = 1.0-sum.r;\n" +
            "       g = 1.0-sum.g;\n" +
            "       b = 1.0-sum.b;\n" +
            "    }else if(mUserMode == 2){\n" +
            "       vec4 sum=make_color();\n" +
            "       r = sum.r;\n" +
            "       g = sum.g;\n" +
            "       b = sum.b;\n" +
            "    }else if(mUserMode == 3){\n" +
            "       vec4 sum=make_color();\n" +
            "       r = 1.0-sum.r;\n" +
            "       g = 1.0-sum.g;\n" +
            "       b = sum.b;\n" +
            "    }else if(mUserMode == 4){\n" +
            "       vec4 sum=make_color();\n" +
            "       r = sum.r;\n" +
            "       g = sum.g;\n" +
            "       b = 1.0-sum.b;\n" +
            "    }else if(mUserMode == 5){\n" +
            "       r = y;\n" +
            "       g = y;\n" +
            "       b = y;\n" +
            "    }else if(mUserMode == 6){\n" +
            "       r = 1.0-y;\n" +
            "       g = 1.0-y;\n" +
            "       b = 1.0-y;\n" +
            "    }else if(mUserMode == 11){\n" +
            "       u = u*mSaturation;\n" +
            "       v = v*mSaturation;\n" +
            "       r = (y + 1.403 * v);\n" +
            "       g = (y - 0.344 * u - 0.714 * v);\n" +
            "       b = (y + 1.770 * u);\n" +
            "       vec3 color = vec3(r, g, b);\n" +
            "       vec3 colors[3];\n" +
            "       colors[0] = vec3(0.,0.,1.);\n" +
            "       colors[1] = vec3(1.,1.,0.);\n" +
            "       colors[2] = vec3(1.,0.,0.);\n" +
            "       float lum = (color.r + color.g + color.b)/3.;\n" +
            "       int idx = int(step(0.5,lum));\n" +
            "       vec3 rgb = mix(colors[idx],colors[idx+1],(lum-float(idx)*0.5)/0.5);\n" +
            "       r = rgb.r;\n" +
            "       g = rgb.g;\n" +
            "       b = rgb.b;\n" +
            "    }else if(mUserMode == 7){\n" +
            "       vec4 n[9];\n" +
            "       make_kernal(n);\n" +
            "       vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);\n" +
            "       vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);\n" +
            "       vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));\n" +
            "       r = 1.0-sobel.r;\n" +
            "       g = 1.0-sobel.g;\n" +
            "       b = 1.0-sobel.b;\n" +
            "    }else if(mUserMode == 8){\n" +
            "       vec4 n[9];\n" +
            "       make_kernal( n);\n" +
            "       vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);\n" +
            "       vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);\n" +
            "       vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));\n" +
            "       r = sobel.r;\n" +
            "       g = sobel.g;\n" +
            "       b = sobel.b;\n" +
            "    }else if(mUserMode == 9){\n" +
            "       vec4 n[9];\n" +
            "       make_kernal(n);\n" +
            "       vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);\n" +
            "       vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);\n" +
            "       vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));\n" +
            "       r = 1.0-sobel.r;\n" +
            "       g = 1.0-sobel.g;\n" +
            "       b = sobel.b;\n" +
            "    }else if(mUserMode == 10){\n" +
            "       vec4 n[9];\n" +
            "       make_kernal(n);\n" +
            "       vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);\n" +
            "       vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);\n" +
            "       vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));\n" +
            "       r = sobel.r;\n" +
            "       g = sobel.g;\n" +
            "       b = 1.0-sobel.b;\n" +
            "    }\n" +
//            "    r = y + 1.403 * v;\n" +
//            "    g = y - 0.344 * u - 0.714 * v;\n" +
//            "    b = y + 1.770 * u;\n" +
            "    vec3 nrgb = vec3(r,g,b);\n" +
//            "    gl_FragColor = vec4(r, g, b, 1.0);\n" +
            "    gl_FragColor = tmp(nrgb);\n" +
            "}";


    //片段着色器
    public static final String FRAGMENTCODE_RGB = "#version 100\n" +
            "precision highp float;\n" +
            "varying vec2 v_texcoord;\n" +
            "uniform lowp sampler2D s_texture;\n" +
            "uniform int mUserMode;\n" +
            "uniform float mSaturation;\n" +
            "uniform float mContrast;\n" +
            "uniform float mBrightness;\n" +
            "uniform vec2 piexlSize;\n" +
            "uniform float aryWeight[9];\n" +
            "uniform vec2 colorValue[3];\n" +//两色模式色彩配置
            "uniform vec2 edgeValue[3];\n" +//描边模式色彩配置
            "uniform vec2 aryOffset[9];\n" +//给出卷积内核中各个元素对应像素相对于待处理像素的纹理坐标偏移量
            "uniform float scaleFactor;\n" +//给出最终求和时的加权因子(为调整亮度)
            "uniform float offsetBase;\n" +////给出卷积内核中各个元素对应像素相对于待处理像素的纹理坐标偏移量 除数
            "void make_kernal(inout vec4 n[9]){\n" +
            "   float w = 5.0 / piexlSize.x;\nfloat h = 5.0 / piexlSize.y;\n" +
            "   vec3 m = texture2D(s_texture, v_texcoord + vec2( -w, -h)).rgb;\n" +
            "   n[0] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_texture, v_texcoord + vec2(0.0, -h)).rgb;\n" +
            "   n[1] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_texture, v_texcoord + vec2(  w, -h)).rgb;\n" +
            "   n[2] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_texture, v_texcoord + vec2( -w, 0.0)).rgb;\n" +
            "   n[3] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_texture, v_texcoord).rgb;\n" +
            "   n[4] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_texture, v_texcoord + vec2(  w, 0.0)).rgb;\n" +
            "   n[5] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_texture, v_texcoord + vec2( -w, h)).rgb;\n" +
            "   n[6] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_texture, v_texcoord + vec2(0.0, h)).rgb;\n" +
            "   n[7] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "   m = texture2D(s_texture, v_texcoord + vec2(  w, h)).rgb;\n" +
            "   n[8] = vec4(0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0.299*m.r+0.587*m.g+0.114*m.b,0);\n" +
            "}\n" +

            "vec4 tmp(vec3 colorrbg){\n" +
            "   float PI = 3.1415926;\n" +
            "   float B = mBrightness;\n" +
            "   float c = mContrast;\n" +
            "   if(mUserMode==5||mUserMode==6){\n" +
            "       c=0.6*c+0.4;\n" +
            "   }\n" +
            "   float k = tan((45.0 + 44.0 * c) / 180.0 * PI);\n" +
            "   colorrbg = ((colorrbg*255.0 - 127.5 * (1.0 - B)) * k + 127.5 * (1.0 + B)) / 255.0;\n" +
            "   return vec4(colorrbg,1.0);\n " +
            "}\n" +

            "vec4 make_color(){\n" +
            "   vec4 sum;" +
            "   for(int i=0;i<9;i++){\n" +
            "       sum+=texture2D(s_texture,v_texcoord.st+aryOffset[i].xy/offsetBase)*aryWeight[i];" +
            "   };\n" +
            "   return sum*scaleFactor;\n" +
            "}\n" +
            "void main() {\n" +
            "    float y, u, v, r, g, b;\n" +
            "    vec3 orgb =  texture2D(s_texture, v_texcoord).rgb;\n" +
            "    r = orgb.r;\n" +
            "    g = orgb.g;\n" +
            "    b = orgb.b;\n" +
            "    y = 0.299*r+0.587*g+0.114*b;\n" +
            "    u = 0.564*(b - y);\n" +
            "    v = 0.713*(r-y);\n " +
            "    if(mUserMode == 0){\n" +
            "       u = u * mSaturation;\n" +
            "       v = v * mSaturation;\n" +
            "       r = (y + 1.403 * v);\n" +
            "       g = (y - 0.344 * u - 0.714 * v);\n" +
            "       b = (y + 1.770 * u);" +
            "    }else if(mUserMode == 1){\n" +
            "       vec4 sum=make_color();\n" +
            "       r = colorValue[0].x + (colorValue[0].y * sum.r);\n" +
            "       g = colorValue[1].x + (colorValue[1].y * sum.g);\n" +
            "       b = colorValue[2].x + (colorValue[2].y * sum.b);\n" +
            "    }else if(mUserMode == 2){\n" +
            "       r = y;\n" +
            "       g = y;\n" +
            "       b = y;\n" +
            "    }else if(mUserMode == 3){\n" +
            "       r = 1.0-y;\n" +
            "       g = 1.0-y;\n" +
            "       b = 1.0-y;\n" +
            "    }else if(mUserMode == 5){\n" +
            "       u = u*mSaturation;\n" +
            "       v = v*mSaturation;\n" +
            "       r = (y + 1.403 * v);\n" +
            "       g = (y - 0.344 * u - 0.714 * v);\n" +
            "       b = (y + 1.770 * u);\n" +
            "       vec3 color = vec3(r, g, b);\n" +
            "       vec3 colors[3];\n" +
            "       colors[0] = vec3(0.,0.,1.);\n" +
            "       colors[1] = vec3(1.,1.,0.);\n" +
            "       colors[2] = vec3(1.,0.,0.);\n" +
            "       float lum = (color.r + color.g + color.b)/3.;\n" +
            "       int idx = int(step(0.5,lum));\n" +
            "       vec3 rgb = mix(colors[idx],colors[idx+1],(lum-float(idx)*0.5)/0.5);\n" +
            "       r = rgb.r;\n" +
            "       g = rgb.g;\n" +
            "       b = rgb.b;\n" +
            "    }else if(mUserMode == 4){\n" +
            "       vec4 n[9];\n" +
            "       make_kernal(n);\n" +
            "       vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);\n" +
            "       vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);\n" +
            "       vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));\n" +
            "       r = edgeValue[0].x + (edgeValue[0].y * sobel.r);\n" +
            "       g = edgeValue[1].x + (edgeValue[1].y * sobel.g);\n" +
            "       b = edgeValue[2].x + (edgeValue[2].y * sobel.b);\n" +
            "    }\n" +
//            "    r = y + 1.403 * v;\n" +
//            "    g = y - 0.344 * u - 0.714 * v;\n" +
//            "    b = y + 1.770 * u;\n" +
            "    vec3 nrgb = vec3(r,g,b);\n" +
//            "    gl_FragColor = vec4(r, g, b, 1.0);\n" +
            "    gl_FragColor = tmp(nrgb);\n" +
            "}";


    //顶点着色器
    public static final String vertex_code = "#version 100\n" +
            "varying vec2 v_texcoord;\n" +
            "attribute vec4 position;\n" +
            "attribute vec2 texcoord;\n" +
            "uniform mat4 MVP;\n" +
            "void main() {\n" +
            "    v_texcoord = texcoord;\n" +
            "    gl_Position = MVP * position;\n" +
            "}";
    //片段着色器
    public static final String fragment_code = "#version 100\n" +
            "precision highp float;\n" +
            "varying vec2 v_texcoord;\n" +
            "uniform lowp sampler2D s_textureY;\n" +
            "uniform lowp sampler2D s_textureU;\n" +
            "uniform lowp sampler2D s_textureV;\n" +
            "void main() {\n" +
            "    float y, u, v, r, g, b;\n" +
            "    y = texture2D(s_textureY, v_texcoord).r;\n" +
            "    u = texture2D(s_textureU, v_texcoord).r;\n" +
            "    v = texture2D(s_textureV, v_texcoord).r;\n" +
            "    u = u - 0.5;\n" +
            "    v = v - 0.5;\n" +
            "    r = y + 1.403 * v;\n" +
            "    g = y - 0.344 * u - 0.714 * v;\n" +
            "    b = y + 1.770 * u;\n" +
            "    gl_FragColor = vec4(r, g, b, 1.0);\n" +
            "}";


}
