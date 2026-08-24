/**
 * Điều hướng sau login theo vai trò — tránh vào trang không đúng quyền (403 API).
 */
export function getPostLoginPath(roleName, requirePasswordChange) {
  if (requirePasswordChange) {
    return "/change-password";
  }

  const role = String(roleName || "").toUpperCase();

  if (role === "ADMIN" || role === "STAFF") {
    return "/home";
  }

  // USER / khách
  return "/home";
}
