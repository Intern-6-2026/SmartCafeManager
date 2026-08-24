/**
 * Điều hướng sau login theo vai trò — tránh vào trang không đúng quyền (403 API).
 */
export function getPostLoginPath(roleName, requirePasswordChange) {
  const role = String(roleName || "").toUpperCase();
  const isInternalStaff = role === "ADMIN" || role === "STAFF";

  if (requirePasswordChange && isInternalStaff) {
    return "/change-password";
  }

  if (role === "ADMIN" || role === "STAFF") {
    return "/home";
  }

  // USER / khách
  return "/home";
}
