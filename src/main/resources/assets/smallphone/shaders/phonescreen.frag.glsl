#version 120

uniform sampler2D u_GameTexture;
uniform sampler2D u_BackgroundTexture;
uniform vec2 u_Resolution;
uniform float u_Time;

varying vec2 v_TexCoord;

const vec2 BG_SIZE = vec2(2560.0, 1440.0);

const mat3 invHomography = mat3(
1.98757398, 0.69139027, 0.52946318,
-0.03897204, 2.29415864, 0.13513074,
-0.22720699, -0.74827284, 1.0
);

void main() {
    vec2 screen_uv = v_TexCoord;
    float window_aspect = u_Resolution.x / u_Resolution.y;
    float texture_aspect = BG_SIZE.x / BG_SIZE.y;
    float scale_x = 1.0;
    float scale_y = 1.0;
    float offset_x = 0.0;
    float offset_y = 0.0;

    if (window_aspect > texture_aspect) {
        scale_y = window_aspect / texture_aspect;
        offset_y = (1.0 - scale_y) / 2.0;
    } else {
        scale_x = texture_aspect / window_aspect;
        offset_x = (1.0 - scale_x) / 2.0;
    }
    vec2 bg_uv = (screen_uv - vec2(offset_x, offset_y)) / vec2(scale_x, scale_y);

    vec4 finalColor = vec4(0.0, 0.0, 0.0, 1.0);

    if (bg_uv.x >= 0.0 && bg_uv.x <= 1.0 && bg_uv.y >= 0.0 && bg_uv.y <= 1.0) {

        vec2 flipped_bg_uv = bg_uv;
        finalColor = texture2D(u_BackgroundTexture, flipped_bg_uv);

        vec3 bg_uvw = vec3(flipped_bg_uv.x, flipped_bg_uv.y, 1.0);
        vec3 game_uvw = invHomography * bg_uvw;
        vec2 game_uv = game_uvw.xy / game_uvw.z;

        float bottom_tolerance = 0.02;

        if (game_uv.x >= 0.0 && game_uv.x <= 1.0 && game_uv.y >= -bottom_tolerance && game_uv.y <= 1.0) {

            game_uv = clamp(game_uv, 0.0, 1.0);

            vec4 gameColor = texture2D(u_GameTexture, game_uv);

            //Moire pattern
            float time_offset = u_Time * 2.0;
            float moire_intensity = 0.04;
            float base_freq = 800.0;
            float tilt_factor = 0.2;
            vec3 moire_color = vec3(
            sin(game_uv.x * base_freq + game_uv.y * base_freq * tilt_factor + time_offset) * moire_intensity,
            sin(game_uv.x * (base_freq + 10.0) - game_uv.y * base_freq * tilt_factor - time_offset * 0.5) * moire_intensity,
            sin(game_uv.x * (base_freq + 20.0) + game_uv.y * base_freq * tilt_factor * 0.5 + time_offset * 0.2) * moire_intensity
            );

            //Refection
            vec4 reflection = texture2D(u_BackgroundTexture, flipped_bg_uv * 0.5 + 0.25);

            //Flash Lamp
            vec2 light_pos = vec2(0.35, 0.7);
            vec3 glare_color = vec3(1.0, 1.0, 1.0);
            float aspect_ratio = u_Resolution.x / u_Resolution.y;
            vec2 scaled_pos = vec2(game_uv.x * aspect_ratio, game_uv.y);
            vec2 scaled_light_pos = vec2(light_pos.x * aspect_ratio, light_pos.y);
            float dist_to_light = distance(scaled_pos, scaled_light_pos);
            float core_brightness = 10.0;
            float core_strength = smoothstep(0.05, 0.03, dist_to_light);
            float halo_brightness = 0.7;
            float halo_strength = smoothstep(0.15, 0.05, dist_to_light);

            //Dazzle
            float haze_power = 2.5;
            float haze_strength = pow(game_uv.y, haze_power);
            float haze_brightness = 0.3;
            vec3 haze_color = vec3(1.0, 1.0, 0.95);

            //Dazzle outside
            vec2 dist_to_edge_xy = min(game_uv, 1.0 - game_uv);
            float dist_to_edge = min(dist_to_edge_xy.x * aspect_ratio, dist_to_edge_xy.y);
            vec3 bleed_color = vec3(0.8, 0.9, 1.0);
            float bleed_strength = smoothstep(0.05, 0.0, dist_to_edge);
            float bleed_base_brightness = 0.25;
            float variation_freq_x = 10.0;
            float variation_freq_y = 6.0;
            float time_mod = u_Time * 0.01;
            float x_var = sin(game_uv.x * variation_freq_x + time_mod) * 0.5 + 0.5;
            float y_var = sin(game_uv.y * variation_freq_y - time_mod) * 0.5 + 0.5;
            float variation_factor = pow(x_var * y_var, 3.0);

            //Mix
            finalColor = gameColor;
            finalColor.rgb += moire_color;
            finalColor.rgb = mix(finalColor.rgb, reflection.rgb, 0.1);
            finalColor.rgb += glare_color * ( (core_strength * core_brightness) + (halo_strength * halo_brightness) );
            finalColor.rgb += haze_color * haze_strength * haze_brightness;
            finalColor.rgb += bleed_color * bleed_strength * bleed_base_brightness * variation_factor;
        }
    }

    gl_FragColor = finalColor;
}
