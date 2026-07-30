import React, { useCallback, useEffect, useState } from "react";
import "../../styles/feedback-manager.css";
import { getFeedbacks, getApiErrorMessage } from "../../services/apiService";

const PAGE_SIZE = 10;

/* Chuẩn hoá 1 phản hồi từ API về shape dùng trong bảng.
   Đọc phòng thủ nhiều tên trường vì chưa chốt DTO với backend. */
const normalize = (f, i) => ({
  id: f.feedbackId ?? f.id ?? i,
  name: f.fullName ?? f.customerName ?? f.name ?? "Khách",
  email: f.email ?? "",
  table: f.tableName ?? "",
  content: f.content ?? f.message ?? f.feedback ?? "",
  createdAt: f.createdAt ?? f.time ?? "",
  images: Array.isArray(f.imageUrls)
    ? f.imageUrls
    : f.imageUrl
    ? [f.imageUrl]
    : [],
});

const fmtTime = (s) => {
  if (!s) return "—";
  const d = new Date(s);
  if (Number.isNaN(d.getTime())) return s;
  return d.toLocaleString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    day: "2-digit",
    month: "2-digit",
  });
};

const initials = (name) =>
  name.split(" ").map((w) => w[0]).slice(-2).join("").toUpperCase();

function FeedbackManager() {
  const [rows, setRows] = useState([]);
  const [page, setPage] = useState(0); // API dùng index từ 0
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [lightbox, setLightbox] = useState(null);

  const loadFeedbacks = useCallback(async (p) => {
    setLoading(true);
    setError("");
    try {
      const res = await getFeedbacks(p, PAGE_SIZE);
      const data = res.data;
      // Hỗ trợ cả Spring Page lẫn mảng thuần
      const list = Array.isArray(data) ? data : data?.content ?? [];
      setRows(list.map(normalize));
      setTotalPages(
        Array.isArray(data) ? 1 : data?.totalPages ?? 1
      );
      setTotalItems(
        Array.isArray(data) ? data.length : data?.totalElements ?? list.length
      );
    } catch (err) {
      setRows([]);
      setTotalPages(0);
      setTotalItems(0);
      setError(getApiErrorMessage(err, "Không tải được danh sách phản hồi."));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadFeedbacks(page);
  }, [page, loadFeedbacks]);

  return (
    <div className="feedback-manager">
      <div className="topbar">
        <div className="brand">
          <div className="brand-mark">N</div>
          <div className="brand-name">NEOCAFÉ</div>
        </div>
        <div className="topbar-right">
          <span>Quản lý phản hồi</span>
        </div>
      </div>

      <div className="wrap">
        <div className="page-head">
          <div className="page-title">Quản lý phản hồi</div>
          <div className="page-count">
            {totalItems > 0 ? `${totalItems} phản hồi` : ""}
          </div>
        </div>

        {error && <div className="fb-error">{error}</div>}

        <div className="table-scroll">
          <table className="fb-table">
            <thead>
              <tr>
                <th style={{ width: "20%" }}>Khách hàng</th>
                <th style={{ width: "9%" }}>Bàn</th>
                <th>Nội dung</th>
                <th style={{ width: "14%" }}>Hình ảnh</th>
                <th style={{ width: "13%" }}>Thời gian</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={5} className="fb-state">Đang tải phản hồi...</td>
                </tr>
              ) : rows.length === 0 ? (
                <tr>
                  <td colSpan={5} className="fb-state">
                    {error ? "Không có dữ liệu để hiển thị." : "Chưa có phản hồi nào."}
                  </td>
                </tr>
              ) : (
                rows.map((r) => (
                  <tr key={r.id}>
                    <td>
                      <div className="cust">
                        <div className="cust-avatar">{initials(r.name)}</div>
                        <div>
                          <div className="cust-name">{r.name}</div>
                          <div className="cust-email">{r.email}</div>
                        </div>
                      </div>
                    </td>
                    <td>{r.table || "—"}</td>
                    <td className="fb-text">{r.content}</td>
                    <td>
                      {r.images.length === 0 ? (
                        <span className="no-img">—</span>
                      ) : (
                        <div className="thumbs">
                          {r.images.map((src, i) => (
                            <button
                              key={i}
                              className="thumb"
                              onClick={() => setLightbox(src)}
                              aria-label={`Xem ảnh ${i + 1} của ${r.name}`}
                            >
                              <img src={src} alt="" />
                            </button>
                          ))}
                        </div>
                      )}
                    </td>
                    <td className="fb-time">{fmtTime(r.createdAt)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {totalPages > 1 && (
          <div className="pagination">
            <button
              className="pg-btn"
              onClick={() => setPage((p) => Math.max(0, p - 1))}
              disabled={page === 0 || loading}
            >
              ← Trước
            </button>
            <div className="pg-nums">
              {Array.from({ length: totalPages }).map((_, i) => (
                <button
                  key={i}
                  className={`pg-num ${page === i ? "active" : ""}`}
                  onClick={() => setPage(i)}
                  disabled={loading}
                >
                  {i + 1}
                </button>
              ))}
            </div>
            <button
              className="pg-btn"
              onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1 || loading}
            >
              Sau →
            </button>
          </div>
        )}
      </div>

      {lightbox && (
        <div className="lightbox" onClick={() => setLightbox(null)}>
          <img src={lightbox} alt="Ảnh phản hồi" onClick={(e) => e.stopPropagation()} />
          <button className="lightbox-close" onClick={() => setLightbox(null)} aria-label="Đóng">
            ✕
          </button>
        </div>
      )}
    </div>
  );
}

export default FeedbackManager;