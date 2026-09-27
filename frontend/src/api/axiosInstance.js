import axios from "axios";
import { normalizeApiResponse } from "../utils/apiResponse";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  headers: {
    "Content-Type": "application/json",
  },
});
axiosInstance.interceptors.request.use((config) => {
  const isAuthEndpoint = config.url?.includes("/auth/");
  const token = localStorage.getItem("token");
  if (token && !isAuthEndpoint) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
api.interceptors.response.use(
  (response) => normalizeApiResponse(response),
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default api;
