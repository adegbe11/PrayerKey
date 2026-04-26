/** @type {import('next').NextConfig} */
const nextConfig = {
  // Gzip / Brotli compression at the Next.js edge layer
  compress: true,

  // Tree-shake icon/chart packages — only import what is used
  experimental: {
    optimizePackageImports: ["lucide-react", "recharts"],
  },

  // Silence optional native deps (pg-native, bufferutil, etc.)
  webpack: (config, { isServer }) => {
    if (isServer) {
      config.externals = config.externals ?? [];
      if (Array.isArray(config.externals)) {
        config.externals.push("pg-native", "bufferutil", "utf-8-validate");
      }
    }
    return config;
  },

  // Allow images from common avatar/CDN sources
  images: {
    remotePatterns: [
      { protocol: "https", hostname: "lh3.googleusercontent.com" },
      { protocol: "https", hostname: "avatars.githubusercontent.com" },
      { protocol: "https", hostname: "res.cloudinary.com" },
    ],
    // Serve WebP/AVIF by default for all next/image usage
    formats: ["image/avif", "image/webp"],
  },

  // Security headers + CWV caching
  async headers() {
    return [
      // ── Immutable cache for hashed Next.js bundles ───────────────────────
      // These filenames contain a content hash, so "immutable" is safe.
      {
        source: "/_next/static/:path*",
        headers: [
          { key: "Cache-Control", value: "public, max-age=31536000, immutable" },
        ],
      },
      // ── Long-lived cache for public static assets ─────────────────────────
      {
        source: "/:file(.*\\.(?:png|jpg|jpeg|webp|avif|svg|ico|woff2|woff|ttf|otf|js|css))",
        headers: [
          { key: "Cache-Control", value: "public, max-age=86400, stale-while-revalidate=604800" },
        ],
      },
      {
        source: "/(.*)",
        headers: [
          { key: "X-Content-Type-Options",    value: "nosniff" },
          { key: "X-Frame-Options",           value: "DENY" },
          { key: "X-XSS-Protection",          value: "1; mode=block" },
          { key: "Referrer-Policy",           value: "strict-origin-when-cross-origin" },
          { key: "Permissions-Policy",        value: "camera=(), microphone=(self), geolocation=()" },
        ],
      },
      // Allow microphone access on the live sermon page
      {
        source: "/live",
        headers: [
          { key: "Permissions-Policy", value: "microphone=(self)" },
        ],
      },
      // Open CORS for REST API v1
      {
        source: "/api/v1/:path*",
        headers: [
          { key: "Access-Control-Allow-Origin",  value: "*" },
          { key: "Access-Control-Allow-Methods", value: "GET, POST, PUT, DELETE, OPTIONS" },
          { key: "Access-Control-Allow-Headers", value: "Content-Type, Authorization" },
        ],
      },
      // Open CORS for mobile auth
      {
        source: "/api/mobile/:path*",
        headers: [
          { key: "Access-Control-Allow-Origin",  value: "*" },
          { key: "Access-Control-Allow-Methods", value: "GET, POST, OPTIONS" },
          { key: "Access-Control-Allow-Headers", value: "Content-Type, Authorization" },
        ],
      },
    ];
  },

  // Redirect bare /admin to /admin (handled in page), keep /api/health public
  async redirects() {
    return [];
  },
};

export default nextConfig;
