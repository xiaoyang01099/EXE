#version 150

uniform sampler2D SceneSampler;
uniform sampler2D MaskSampler;
uniform sampler2D NoiseSampler;
uniform sampler2D EndSkySampler;
uniform sampler2D EndPortalSampler;
uniform sampler2D CosmicSampler;
uniform mat4 CosmicUV0;
uniform mat4 CosmicUV1;
uniform mat4 CosmicUV2;
uniform mat4 InverseViewProjection;
uniform vec2 ScreenSize;
uniform float EffectTime;
uniform float DistortionPixels;
uniform float Brightness;
uniform float DebugView;
in vec2 texCoord;
out vec4 fragColor;

const vec3 COLORS[7] = vec3[7](
    vec3(0.022087, 0.098399, 0.110818), vec3(0.011892, 0.095924, 0.089485),
    vec3(0.027636, 0.101689, 0.100326), vec3(0.046564, 0.109883, 0.114838),
    vec3(0.064901, 0.117696, 0.097189), vec3(0.063761, 0.086895, 0.123646),
    vec3(0.084817, 0.111994, 0.166380)
);
const mat4 SCALE_TRANSLATE = mat4(
    0.5, 0.0, 0.0, 0.25, 0.0, 0.5, 0.0, 0.25,
    0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0
);

mat4 portalLayer(float layer) {
    float gameTime = EffectTime / 1200.0;
    mat4 translate = mat4(
        1.0, 0.0, 0.0, 17.0 / layer,
        0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (gameTime * 10.5),
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    float angle = radians((layer * layer * 4321.0 + layer * 9.0) * 2.0);
    mat2 rotate = mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
    mat2 scale = mat2((4.5 - layer / 4.0) * 2.0);
    return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
}

vec3 cosmicHsv(vec3 c) {
    vec3 p=abs(fract(c.xxx+vec3(0.0,2.0/3.0,1.0/3.0))*6.0-3.0);
    return c.z*mix(vec3(1),clamp(p-1.0,0.0,1.0),c.y);
}
mat3 cosmicRotation(vec3 axis,float angle) {
    axis=normalize(axis); float s=sin(angle),c=cos(angle),o=1.0-c;
    return mat3(o*axis.x*axis.x+c,o*axis.x*axis.y+axis.z*s,o*axis.x*axis.z-axis.y*s,
        o*axis.x*axis.y-axis.z*s,o*axis.y*axis.y+c,o*axis.y*axis.z+axis.x*s,
        o*axis.x*axis.z+axis.y*s,o*axis.y*axis.z-axis.x*s,o*axis.z*axis.z+c);
}
// Same layered spherical glyph projection and atlas as the existing rainbow cosmic material.
vec3 cosmicBlade(vec3 direction) {
    const float PI=3.14159265359;
    float time=EffectTime*20.0;
    vec3 color=vec3(.016,.022,.055);
    for(int i=0;i<16;i++) {
        int mult=16-i,j=i+7,k=j+1;
        float r1=float((j*j*4321+j*8)*2),r2=float((k*k*k*239+k*37)*3),r3=r1*347.4+r2*63.4;
        vec3 ray=cosmicRotation(vec3(sin(r1),sin(r2),cos(r3)),mod(r3,2.0*PI))*direction;
        vec2 sphere=vec2(.5+atan(ray.z,ray.x)/(2.0*PI),.5+asin(clamp(ray.y,-1.0,1.0))/PI);
        float scale=float(mult)*.5+1.8264;
        vec2 uv=(sphere+vec2(0,time*.0003420))*vec2(scale,scale*.5)*16.0;
        ivec2 tile=ivec2(mod(floor(uv),16.0));
        int hash=((1777541*tile.x)+(7649689*tile.y)+(361273*(i+31))+1723609)^50943779;
        int symbol=int(mod(float(hash),50.0));
        if(symbol>=11) continue;
        vec2 cell=fract(uv); int turn=(hash>>8)&3;
        if((hash&128)!=0) cell.x=1.0-cell.x;
        if(turn==1) cell=vec2(1.0-cell.y,cell.x);
        else if(turn==2) cell=1.0-cell;
        else if(turn==3) cell=vec2(cell.y,1.0-cell.x);
        vec4 bounds=symbol<4?CosmicUV0[symbol]:(symbol<8?CosmicUV1[symbol-4]:CosmicUV2[symbol-8]);
        vec4 glyph=texture(CosmicSampler,mix(bounds.xy,bounds.zw,cell));
        float a=glyph.r*glyph.a*(.5+1.0/float(mult))*(1.0-smoothstep(.20,.48,abs(sphere.y-.5)));
        color+=cosmicHsv(vec3(fract(time*.00246+float(i)*.0924+sphere.x*.05+sphere.y*.05),.45,1.3))*a;
    }
    return color;
}

void main() {
    vec4 scene = texture(SceneSampler, texCoord);
    vec4 mask = texture(MaskSampler, texCoord);
    bool cosmic=mask.b>=.5;
    float strength=clamp(cosmic?(mask.b-.5)*2.0:mask.b/.49,0.0,1.0);
    vec2 px = 1.0 / ScreenSize;
    vec4 left = texture(MaskSampler, texCoord - vec2(px.x, 0.0));
    vec4 right = texture(MaskSampler, texCoord + vec2(px.x, 0.0));
    vec4 down = texture(MaskSampler, texCoord - vec2(0.0, px.y));
    vec4 up = texture(MaskSampler, texCoord + vec2(0.0, px.y));
    vec2 direction = vec2(
        (right.a > 0.0 ? right.r : mask.r) - (left.a > 0.0 ? left.r : mask.r),
        (up.a > 0.0 ? up.r : mask.r) - (down.a > 0.0 ? down.r : mask.r)
    );
    direction /= max(length(direction), 0.00001);
    if (DebugView > 1.5) {
        fragColor = vec4(mask.rgb * mask.a, 1.0);
        return;
    }
    if (DebugView > 0.5) {
        fragColor = vec4(vec3(mask.a * strength), 1.0);
        return;
    }
    if (mask.a < 0.004) {
        fragColor = scene;
        return;
    }

    float age = mask.r;
    float width = mask.g;
    float fade = pow(max(1.0 - age, 0.0), 1.4);
    float edgeFactor = 0.5 / (2.0 - 1.5 * width);
    vec2 localUV = vec2(age, width);
    float noise = texture(NoiseSampler, fract(localUV * 2.0 - vec2(EffectTime * 1.8, 0.0))).r;
    float noiseFactor = 0.2 + 0.8 * noise + 1.0 - smoothstep(0.0, 0.8, age);
    float coverage = mask.a * strength;
    float alpha = clamp(edgeFactor * fade * noiseFactor, 0.0, 1.0) * coverage;

    vec4 projected = vec4(texCoord, 0.0, 1.0);
    vec3 color = texture(EndSkySampler, texCoord).rgb * COLORS[0];
    for (int i = 0; i < 7; i++) {
        vec4 p = projected * portalLayer(float(i + 1));
        vec2 portalUV = fract(0.2 * (p.xy / p.w));
        color += 3.0 * texture(EndPortalSampler, portalUV).rgb * COLORS[i];
    }
    color += vec3(0.03, 0.27, 0.27) * smoothstep(0.3, 0.5, abs(edgeFactor - 0.5));
    if(cosmic) {
        vec4 nearPoint=InverseViewProjection*vec4(texCoord*2.0-1.0,-1,1);
        vec4 farPoint=InverseViewProjection*vec4(texCoord*2.0-1.0,1,1);
        vec3 direction=normalize(farPoint.xyz/farPoint.w-nearPoint.xyz/nearPoint.w);
        color=cosmicBlade(direction)+vec3(.30,.18,.65)*pow(width,10.0)*fade;
        alpha=max(alpha,coverage*fade*.88);
    }
    vec2 offset = direction * DistortionPixels * (1.0 - noiseFactor) * fade * coverage / ScreenSize;
    vec2 sceneUV = clamp(texCoord + offset, px * 0.5, 1.0 - px * 0.5);
    vec3 refracted = texture(SceneSampler, sceneUV).rgb;
    fragColor = vec4(mix(refracted, color * Brightness, alpha), scene.a);
}
