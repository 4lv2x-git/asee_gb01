class NeuralGlow {
    constructor(canvasSelector, options = {}) {
        this.canvas = document.querySelector(canvasSelector);
        this.gl = null;
        this.program = null;
        this.uniforms = {};
        this.animationId = null;
        this.primaryColor = options.primaryColor || [0.1, 0.2, 0.8];
        this.secondaryColor = options.secondaryColor || [0.0, 0.1, 0.4];
        this.pointer = { x: 0, y: 0, tX: 0, tY: 0 };

        this.vertexShaderSource = `
            precision mediump float;
            varying vec2 vUv;
            attribute vec2 a_position;
            void main() {
                vUv = .5 * (a_position + 1.);
                gl_Position = vec4(a_position, 0.0, 1.0);
            }
        `;

        this.fragmentShaderSource = `
            precision mediump float;
            varying vec2 vUv;
            uniform float u_time;
            uniform float u_ratio;
            uniform vec2 u_pointer_position;
            uniform float u_scroll_progress;

            vec2 rotate(vec2 uv, float th) {
                return mat2(cos(th), sin(th), -sin(th), cos(th)) * uv;
            }

            float neuro_shape(vec2 uv, float t, float p) {
                vec2 sine_acc = vec2(0.);
                vec2 res = vec2(0.);
                float scale = 8.;
                for (int j = 0; j < 15; j++) {
                    uv = rotate(uv, 1.);
                    sine_acc = rotate(sine_acc, 1.);
                    vec2 layer = uv * scale + float(j) + sine_acc - t;
                    sine_acc += sin(layer) + 2.4 * p;
                    res += (.5 + .5 * cos(layer)) / scale;
                    scale *= 1.2;
                }
                return res.x + res.y;
            }

            void main() {
                vec2 uv = .5 * vUv;
                uv.x *= u_ratio;
                vec2 pointer = vUv - u_pointer_position;
                pointer.x *= u_ratio;
                float p = clamp(length(pointer), 0., 1.);
                p = .5 * pow(1. - p, 2.);
                float t = .001 * u_time;
                float noise = neuro_shape(uv, t, p);
                noise = 1.2 * pow(noise, 3.);
                noise += pow(noise, 10.);
                noise = max(.0, noise - .5);
                noise *= (1. - length(vUv - .5));
                vec3 color = vec3({{PRIMARY_R}}, {{PRIMARY_G}}, {{PRIMARY_B}});
                color += vec3({{SECONDARY_R}}, {{SECONDARY_G}}, {{SECONDARY_B}})
                    * sin(3.0 * u_scroll_progress + 1.5);
                color *= noise;
                gl_FragColor = vec4(color, noise);
            }
        `;
    }

    createShader(source, type) {
        const shader = this.gl.createShader(type);
        this.gl.shaderSource(shader, source);
        this.gl.compileShader(shader);
        if (!this.gl.getShaderParameter(shader, this.gl.COMPILE_STATUS)) {
            this.gl.deleteShader(shader);
            return null;
        }
        return shader;
    }

    initShader() {
        if (!this.canvas) return false;
        this.gl = this.canvas.getContext("webgl") || this.canvas.getContext("experimental-webgl");
        if (!this.gl) return false;

        const fragmentSource = this.fragmentShaderSource
            .replace("{{PRIMARY_R}}", this.primaryColor[0])
            .replace("{{PRIMARY_G}}", this.primaryColor[1])
            .replace("{{PRIMARY_B}}", this.primaryColor[2])
            .replace("{{SECONDARY_R}}", this.secondaryColor[0])
            .replace("{{SECONDARY_G}}", this.secondaryColor[1])
            .replace("{{SECONDARY_B}}", this.secondaryColor[2]);

        const vertex = this.createShader(this.vertexShaderSource, this.gl.VERTEX_SHADER);
        const fragment = this.createShader(fragmentSource, this.gl.FRAGMENT_SHADER);
        this.program = this.gl.createProgram();
        this.gl.attachShader(this.program, vertex);
        this.gl.attachShader(this.program, fragment);
        this.gl.linkProgram(this.program);
        this.gl.useProgram(this.program);

        const vertices = new Float32Array([-1, -1, 1, -1, -1, 1, 1, 1]);
        const buffer = this.gl.createBuffer();
        this.gl.bindBuffer(this.gl.ARRAY_BUFFER, buffer);
        this.gl.bufferData(this.gl.ARRAY_BUFFER, vertices, this.gl.STATIC_DRAW);

        const position = this.gl.getAttribLocation(this.program, "a_position");
        this.gl.enableVertexAttribArray(position);
        this.gl.vertexAttribPointer(position, 2, this.gl.FLOAT, false, 0, 0);

        this.uniforms.u_time = this.gl.getUniformLocation(this.program, "u_time");
        this.uniforms.u_ratio = this.gl.getUniformLocation(this.program, "u_ratio");
        this.uniforms.u_pointer_position = this.gl.getUniformLocation(this.program, "u_pointer_position");
        this.uniforms.u_scroll_progress = this.gl.getUniformLocation(this.program, "u_scroll_progress");
        return true;
    }

    resize = () => {
        if (!this.gl) return;
        const ratio = Math.min(window.devicePixelRatio, 2);
        this.canvas.width = window.innerWidth * ratio;
        this.canvas.height = window.innerHeight * ratio;
        this.gl.uniform1f(this.uniforms.u_ratio, this.canvas.width / this.canvas.height);
        this.gl.viewport(0, 0, this.canvas.width, this.canvas.height);
    };

    render = () => {
        if (!this.gl) return;
        this.pointer.x += (this.pointer.tX - this.pointer.x) * 0.2;
        this.pointer.y += (this.pointer.tY - this.pointer.y) * 0.2;
        this.gl.uniform1f(this.uniforms.u_time, performance.now());
        this.gl.uniform2f(this.uniforms.u_pointer_position,
            this.pointer.x / window.innerWidth,
            1 - this.pointer.y / window.innerHeight);
        this.gl.uniform1f(this.uniforms.u_scroll_progress,
            window.pageYOffset / (2 * window.innerHeight));
        this.gl.drawArrays(this.gl.TRIANGLE_STRIP, 0, 4);
        this.animationId = requestAnimationFrame(this.render);
    };

    handlePointerMove = event => {
        this.pointer.tX = event.clientX;
        this.pointer.tY = event.clientY;
    };

    init() {
        if (!this.initShader()) return;
        this.resize();
        this.render();
        window.addEventListener("resize", this.resize);
        window.addEventListener("pointermove", this.handlePointerMove);
    }
}
