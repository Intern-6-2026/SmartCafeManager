export function formatNewsDate(value) {
  if (!value) return "—";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return "—";
  return d.toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export const NEWS_STATUS_LABEL = {
  PENDING: "Chờ duyệt",
  PUBLISHED: "Đã đăng",
  REJECTED: "Từ chối",
};

export function getRoleName() {
  return (localStorage.getItem("roleName") || "").toUpperCase();
}

export function getCurrentUsername() {
  return localStorage.getItem("userName") || "";
}

export function canManageNews() {
  const role = getRoleName();
  return role === "ADMIN" || role === "STAFF";
}

export function isAdminRole() {
  return getRoleName() === "ADMIN";
}

export function isStaffRole() {
  return getRoleName() === "STAFF";
}

/** Admin sửa/xóa mọi bài; Staff chỉ bài do chính mình viết */
export function canEditOrDeleteNews(authorUsername) {
  if (isAdminRole()) return true;
  if (!isStaffRole()) return false;
  const me = getCurrentUsername();
  if (!me || !authorUsername) return false;
  return me.toLowerCase() === String(authorUsername).toLowerCase();
}
