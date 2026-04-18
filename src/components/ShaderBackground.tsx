'use client';

import { useEffect, useRef } from 'react';

/**
 * Awwwards-tier animated WebGL background.
 * Pure WebGL — no Three.js.
 * Renders a full-viewport quad with a fragment shader that combines:
 *   - layered fBm noise driven by time + mouse
 *   - two soft radial gradients (purple + yellow)
 *   - subtle grain
 * Falls back silently to nothing if WebGL isn't available.
 */
export default function ShaderBackground({ className, style }: { className?: string; style?: React.CSSProperties }) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const gl =
      canvas.getContext('webgl', { premultipliedAlpha: true, antialias: false }) ||
      (canvas.getContext('experimental-webgl') as WebGLRenderingContext | null);
    if (!gl) return;

    const vertSrc = `
      attribute vec2 a_pos;
      void main() {
        gl_Position = vec4(a_pos, 0.0, 1.0);
      }
    `;

    // Smooth, layered noise + two radial blobs. No external textures.
    const fragSrc = `
      precision mediump float;
      uniform vec2 u_res;
      uniform float u_time;
      uniform vec2 u_mouse;

      // hash -> 2d noise
      float hash(vec2 p) {
        return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
      }
      float noise(vec2 p) {
        vec2 i = floor(p);
        vec2 f = fract(p);
        float a = hash(i);
        float b = hash(i + vec2(1.0, 0.0));
        float c = hash(i + vec2(0.0, 1.0));
        float d = hash(i + vec2(1.0, 1.0));
        vec2 u = f*f*(3.0-2.0*f);
        return mix(a, b, u.x) + (c - a)*u.y*(1.0 - u.x) + (d - b)*u.x*u.y;
      }
      float fbm(vec2 p) {
        float v = 0.0;
        float a = 0.5;
        for (int i = 0; i < 5; i++) {
          v += a * noise(p);
          p = p * 2.02 + 17.0;
          a *= 0.5;
        }
        return v;
      }

      void main() {
        vec2 uv = gl_FragCoord.xy / u_res.xy;
        vec2 p = uv;
        p.x *= u_res.x / u_res.y;

        // animated warp
        float t = u_time * 0.06;
        vec2 q = vec2(fbm(p * 1.2 + t), fbm(p * 1.2 - t + 5.2));
        float n = fbm(p * 2.0 + q * 1.4 + t * 1.3);

        // deep purple base
        vec3 base = mix(vec3(0.035, 0.03, 0.07), vec3(0.08, 0.05, 0.18), n);

        // purple aurora (top-left)
        vec2 c1 = vec2(0.22, 0.78);
        float d1 = distance(uv, c1);
        float g1 = smoothstep(0.75, 0.05, d1);
        base += vec3(0.49, 0.23, 0.93) * g1 * (0.35 + 0.15 * n);

        // gold aurora (bottom-right), responds to mouse
        vec2 c2 = vec2(0.78 + (u_mouse.x - 0.5) * 0.2, 0.18 + (u_mouse.y - 0.5) * 0.2);
        float d2 = distance(uv, c2);
        float g2 = smoothstep(0.55, 0.02, d2);
        base += vec3(1.0, 0.9, 0.0) * g2 * (0.14 + 0.08 * n);

        // pink hint
        vec2 c3 = vec2(0.6, 0.4);
        float d3 = distance(uv, c3);
        float g3 = smoothstep(0.6, 0.1, d3);
        base += vec3(1.0, 0.18, 0.47) * g3 * 0.05 * n;

        // grain
        float grain = (hash(gl_FragCoord.xy + u_time) - 0.5) * 0.03;
        base += grain;

        gl_FragColor = vec4(base, 1.0);
      }
    `;

    function compile(type: number, src: string): WebGLShader | null {
      const sh = gl!.createShader(type);
      if (!sh) return null;
      gl!.shaderSource(sh, src);
      gl!.compileShader(sh);
      if (!gl!.getShaderParameter(sh, gl!.COMPILE_STATUS)) {
        console.warn(gl!.getShaderInfoLog(sh));
        gl!.deleteShader(sh);
        return null;
      }
      return sh;
    }

    const vs = compile(gl.VERTEX_SHADER, vertSrc);
    const fs = compile(gl.FRAGMENT_SHADER, fragSrc);
    if (!vs || !fs) return;

    const program = gl.createProgram();
    if (!program) return;
    gl.attachShader(program, vs);
    gl.attachShader(program, fs);
    gl.linkProgram(program);
    if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
      console.warn(gl.getProgramInfoLog(program));
      return;
    }
    gl.useProgram(program);

    // Fullscreen quad
    const buf = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, buf);
    gl.bufferData(
      gl.ARRAY_BUFFER,
      new Float32Array([-1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, 1]),
      gl.STATIC_DRAW
    );
    const posLoc = gl.getAttribLocation(program, 'a_pos');
    gl.enableVertexAttribArray(posLoc);
    gl.vertexAttribPointer(posLoc, 2, gl.FLOAT, false, 0, 0);

    const uRes = gl.getUniformLocation(program, 'u_res');
    const uTime = gl.getUniformLocation(program, 'u_time');
    const uMouse = gl.getUniformLocation(program, 'u_mouse');

    let mouseX = 0.5;
    let mouseY = 0.5;
    const onMouse = (e: MouseEvent) => {
      mouseX = e.clientX / window.innerWidth;
      mouseY = 1 - e.clientY / window.innerHeight;
    };
    window.addEventListener('mousemove', onMouse, { passive: true });

    const resize = () => {
      const dpr = Math.min(1.8, window.devicePixelRatio || 1);
      const w = Math.floor(canvas.clientWidth * dpr);
      const h = Math.floor(canvas.clientHeight * dpr);
      if (canvas.width !== w || canvas.height !== h) {
        canvas.width = w;
        canvas.height = h;
      }
      gl!.viewport(0, 0, canvas.width, canvas.height);
      gl!.uniform2f(uRes, canvas.width, canvas.height);
    };
    resize();
    const ro = new ResizeObserver(resize);
    ro.observe(canvas);

    const start = performance.now();
    let rafId = 0;
    let visible = true;
    const onVis = () => { visible = !document.hidden; };
    document.addEventListener('visibilitychange', onVis);

    const frame = () => {
      if (visible) {
        gl!.uniform1f(uTime, (performance.now() - start) / 1000);
        gl!.uniform2f(uMouse, mouseX, mouseY);
        gl!.drawArrays(gl!.TRIANGLES, 0, 6);
      }
      rafId = requestAnimationFrame(frame);
    };
    rafId = requestAnimationFrame(frame);

    return () => {
      cancelAnimationFrame(rafId);
      ro.disconnect();
      window.removeEventListener('mousemove', onMouse);
      document.removeEventListener('visibilitychange', onVis);
      gl!.deleteBuffer(buf);
      gl!.deleteProgram(program);
      gl!.deleteShader(vs);
      gl!.deleteShader(fs);
    };
  }, []);

  return (
    <canvas
      ref={canvasRef}
      aria-hidden="true"
      className={className}
      style={{
        position: 'absolute',
        inset: 0,
        width: '100%',
        height: '100%',
        pointerEvents: 'none',
        opacity: 0.9,
        ...style,
      }}
    />
  );
}
