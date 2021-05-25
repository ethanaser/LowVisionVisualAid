#version 100
    precision mediump float;
    uniform sampler2D sTexture;
    varying highp vec2 vTextureCoord;
    void main() {
        float y,u,v,r,g,b;
        vec3 orgb = texture(sTexture, vTextureCoord).rgb;
        g = orgb.g;
        b = orgb.b;
        y = 0.299*r+0.587*g+0.114*b;
        u = 0.564*(b - y);
        v = 0.713*(r-y);
        r = y;
        g = y;
        b = y;
        gl_FragColor = vec4(r,g,b, 1.0);
    }
