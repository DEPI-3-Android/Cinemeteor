import React from "react";
import { createRoot } from "react-dom/client";
import { DownloadApp } from "./download_app";

const rootEl = document.getElementById("root");

if (!rootEl) {
  throw new Error("Root element #root not found");
}

createRoot(rootEl).render(
  <React.StrictMode>
    <DownloadApp />
  </React.StrictMode>
);

