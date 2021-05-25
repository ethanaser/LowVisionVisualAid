#version 100
#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES sTexture;
varying highp vec2 vTextureCoord;
uniform int mUserMode;
uniform int mSaturation;

void make_kernel(inout vec4 n[9], samplerExternalOES tex, vec2 coord){
    float w = 5.0 / 1000.0;
    float h = 5.0 / 1000.0;
    vec3 m = texture2D(tex, coord + vec2(-w, -h)).rgb;
    n[0] = vec4(0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0);

    m = texture2D(tex, coord + vec2(0.0, -h)).rgb;
    n[1] = vec4(0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0);

    m = texture2D(tex, coord + vec2(w, -h)).rgb;
    n[2] = vec4(0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0);

    m = texture2D(tex, coord + vec2(-w, 0.0)).rgb;
    n[3] = vec4(0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0);

    m = texture2D(tex, coord).rgb;
    n[4] = vec4(0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0);

    m = texture2D(tex, coord + vec2(w, 0.0)).rgb;
    n[5] = vec4(0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0);

    m = texture2D(tex, coord + vec2(-w, h)).rgb;
    n[6] = vec4(0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0);

    m = texture2D(tex, coord + vec2(0.0, h)).rgb;
    n[7] = vec4(0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0);

    m = texture2D(tex, coord + vec2(w, h)).rgb;
    n[8] = vec4(0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0.299*m.r+0.587*m.g+0.114*m.b, 0);
}

void main() {
    float y, u, v, r, g, b;

    if(mUserMode == 1){
        vec2 offset0=vec2(-1.0,-1.0); vec2 offset1=vec2(0.0,-1.0); vec2 offset2=vec2(1.0,-1.0);
        vec2 offset3=vec2(-1.0,0.0); vec2 offset4=vec2(0.0,0.0); vec2 offset5=vec2(1.0,0.0);
        vec2 offset6=vec2(-1.0,1.0); vec2 offset7=vec2(0.0,1.0); vec2 offset8=vec2(1.0,1.0);
        const float scaleFactor = 0.99;
        float kernelValue0 = 0.0; float kernelValue1 = 1.0; float kernelValue2 = 0.0;
        float kernelValue3 = 1.0; float kernelValue4 = -4.0; float kernelValue5 = 1.0;
        float kernelValue6 = 0.0; float kernelValue7 = 1.0; float kernelValue8 = 0.0;
        vec4 sum;
        vec4 cTemp0,cTemp1,cTemp2,cTemp3,cTemp4,cTemp5,cTemp6,cTemp7,cTemp8;
        cTemp0=texture2D(sTexture, vTextureCoord.st + offset0.xy/512.0);
        cTemp1=texture2D(sTexture, vTextureCoord.st + offset1.xy/512.0);
        cTemp2=texture2D(sTexture, vTextureCoord.st + offset2.xy/512.0);
        cTemp3=texture2D(sTexture, vTextureCoord.st + offset3.xy/512.0);
        cTemp4=texture2D(sTexture, vTextureCoord.st + offset4.xy/512.0);
        cTemp5=texture2D(sTexture, vTextureCoord.st + offset5.xy/512.0);
        cTemp6=texture2D(sTexture, vTextureCoord.st + offset6.xy/512.0);
        cTemp7=texture2D(sTexture, vTextureCoord.st + offset7.xy/512.0);
        cTemp8=texture2D(sTexture, vTextureCoord.st + offset8.xy/512.0);
        //颜色求和
        sum =kernelValue0*cTemp0+kernelValue1*cTemp1+kernelValue2*cTemp2+
        kernelValue3*cTemp3+kernelValue4*cTemp4+kernelValue5*cTemp5+
        kernelValue6*cTemp6+kernelValue7*cTemp7+kernelValue8*cTemp8;
        gl_FragColor = sum * scaleFactor; //进行
        return;
    }

    vec3 orgb = texture2D(sTexture, vTextureCoord).rgb;
    r = orgb.r;
    g = orgb.g;
    b = orgb.b;
    y = 0.299*r+0.587*g+0.114*b;
    u = 0.564*(b - y);
    v = 0.713*(r-y);

    if (mUserMode == 0){
        r = (y + 1.403 * v);
        g = (y - 0.344 * u - 0.714 * v);
        b = (y + 1.770 * u);
    } else if (mUserMode == 5){//grey
        r = y;
        g = y;
        b = y;
    } else if (mUserMode == 6){//anti grey
        r = 1.0-y;
        g = 1.0-y;
        b = 1.0-y;
    } else if (mUserMode == 7){
        vec4 n[9];
        make_kernel(n,sTexture, vTextureCoord);
        vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);
        vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);
        vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));
        r = 1.0-sobel.r;
        g = 1.0-sobel.g;
        b = 1.0-sobel.b;
    } else if( mUserMode == 8){
        vec4 n[9];
        make_kernel(n,sTexture, vTextureCoord);
        vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);
        vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);
        vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));
        r = sobel.r;
        g = sobel.g;
        b = sobel.b;
    } else if( mUserMode == 9){
        vec4 n[9];
        make_kernel(n,sTexture, vTextureCoord);
        vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);
        vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);
        vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));
        r = 1.0-sobel.r;
        g = 1.0-sobel.g;
        b = sobel.b;
    } else if( mUserMode == 10){
        vec4 n[9];
        make_kernel(n,sTexture, vTextureCoord);
        vec4 sobel_edge_h = n[2] + (2.0*n[5]) + n[8] - (n[0] + (2.0*n[3]) + n[6]);
        vec4 sobel_edge_v = n[0] + (2.0*n[1]) + n[2] - (n[6] + (2.0*n[7]) + n[8]);
        vec4 sobel = sqrt((sobel_edge_h * sobel_edge_h) + (sobel_edge_v * sobel_edge_v));
        r = sobel.r;
        g = sobel.g;
        b = 1.0-sobel.b;
    } else if( mUserMode == 11){
        r = (y + 1.403 * v);
        g = (y - 0.344 * u - 0.714 * v);
        b = (y + 1.770 * u);
        vec3 color = vec3(r, g, b);
        vec3 colors[3];
        colors[0] = vec3(0., 0., 1.);
        colors[1] = vec3(1., 1., 0.);
        colors[2] = vec3(1., 0., 0.);
        float lum = (color.r + color.g + color.b)/3.;
        int idx = int(step(0.5, lum));
        vec3 rgb = mix(colors[idx], colors[idx+1], (lum-float(idx)*0.5)/0.5);
        r = rgb.r;
        g = rgb.g;
        b = rgb.b;
    }
    gl_FragColor = vec4(r, g, b, 1.0);
}