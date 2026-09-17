#version 330
uniform sampler2D SourceSampler;
in vec2 texCoord;
out vec4 fragColor;
void main(){fragColor=texture(SourceSampler,texCoord);}
