/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'export',
  trailingSlash: true,
  eslint: { ignoreDuringBuilds: true },
  typescript: { ignoreBuildErrors: true },
  staticPageGenerationTimeout: 180,
  images: {
    unoptimized: true,
    formats: ["image/webp"],
    remotePatterns: [
      { protocol: "https", hostname: "cdn.aigeneratedit.com" },
      { protocol: "https", hostname: "images.unsplash.com" },
    ],
  },
};

export default nextConfig;
