import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite"; // <-- Dòng này quan trọng

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(), // <-- Dòng này kích hoạt Tailwind v4
  ],
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
