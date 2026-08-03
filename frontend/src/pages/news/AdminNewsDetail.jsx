import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Header from "../../components/header";
import Footer from "../../components/footer";
import {
  getAdminNewsById,
  deleteNews,
  changeNewsStatus,
  getApiErrorMessage,
} from "../../services/apiService";
import {
  formatNewsDate,
  NEWS_STATUS_LABEL,
  isAdminRole,
  canEditOrDeleteNews,
} from "../../utils/newsHelpers";
import "../../styles/news.css";

export default function AdminNewsDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const admin = isAdminRole();
  const [news, setNews] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [lightboxOpen, setLightboxOpen] = useState(false);

  const load = () => {
    setLoading(true);
    setError("");
    getAdminNewsById(id)
      .then((res) => setNews(res.data))
      .catch((err) =>
        setError(getApiErrorMessage(err, "Không tìm thấy bài viết."))
      )
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  useEffect(() => {
    if (!lightboxOpen) return undefined;
    const onKey = (e) => {
      if (e.key === "Escape") setLightboxOpen(false);
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [lightboxOpen]);

  const handleDelete = async () => {
    if (!window.confirm("Bạn chắc chắn muốn xóa tin tức này?")) return;
    setBusy(true);
    try {
      await deleteNews(id);
      navigate("/admin/news");
    } catch (err) {
      alert(getApiErrorMessage(err, "Xóa tin thất bại."));
      setBusy(false);
    }
  };

  const handleStatus = async (status) => {
    setBusy(true);
    try {
      await changeNewsStatus(id, status);
      load();
    } catch (err) {
      alert(getApiErrorMessage(err, "Đổi trạng thái thất bại."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <Header />
      <main className="news-page">
        <div className="wrap">
          <Link to="/admin/news" className="news-detail-back">
            ← Quay lại quản lý
          </Link>

          {loading && <div className="news-loading">Đang tải bài viết…</div>}
          {!loading && error && <div className="news-error">{error}</div>}

          {!loading && !error && news && (
            <>
              <div className="page-head">
                <div>
                  <h1 className="page-title">Chi tiết tin tức</h1>
                  <p className="page-sub">
                    {formatNewsDate(news.createdAt)} · Người đăng:{" "}
                    <strong>{news.authorUsername || "—"}</strong> ·{" "}
                    <span className={`status-badge status-${(news.status || "").toLowerCase()}`}>
                      {NEWS_STATUS_LABEL[news.status] || news.status}
                    </span>
                  </p>
                  {!canEditOrDeleteNews(news.authorUsername) && !admin && (
                    <p className="page-sub">
                      Bạn chỉ được xem bài này — không thể sửa hoặc xóa vì không phải tác giả.
                    </p>
                  )}
                </div>
                <div className="page-head-actions">
                  {canEditOrDeleteNews(news.authorUsername) && (
                    <Link
                      to={`/admin/news/${id}/edit`}
                      className="news-btn news-btn-ghost"
                    >
                      Sửa
                    </Link>
                  )}
                  {canEditOrDeleteNews(news.authorUsername) && (
                    <button
                      type="button"
                      className="news-btn news-btn-danger"
                      disabled={busy}
                      onClick={handleDelete}
                    >
                      Xóa
                    </button>
                  )}
                  {admin && news.status !== "PUBLISHED" && (
                    <button
                      type="button"
                      className="news-btn news-btn-primary"
                      disabled={busy}
                      onClick={() => handleStatus("PUBLISHED")}
                    >
                      Duyệt đăng
                    </button>
                  )}
                  {admin && news.status === "PUBLISHED" && (
                    <button
                      type="button"
                      className="news-btn"
                      disabled={busy}
                      onClick={() => handleStatus("REJECTED")}
                    >
                      Ẩn bài
                    </button>
                  )}
                  {news.status === "PUBLISHED" && (
                    <Link to={`/news/${id}`} className="news-btn news-btn-ghost">
                      Xem phía khách
                    </Link>
                  )}
                </div>
              </div>

              <article className="news-detail">
                {news.imageUrl ? (
                  <img
                    className="news-detail-hero"
                    src={news.imageUrl}
                    alt={news.title}
                    onClick={() => setLightboxOpen(true)}
                  />
                ) : (
                  <div className="news-detail-hero" aria-hidden="true" />
                )}
                <div className="news-detail-body">
                  <h2 className="news-detail-title">{news.title}</h2>
                  {news.summary ? (
                    <p className="news-detail-summary">{news.summary}</p>
                  ) : null}
                  <div className="news-detail-content">{news.content}</div>
                </div>
              </article>
            </>
          )}
        </div>
      </main>

      {news?.imageUrl ? (
        <div
          className={`news-lightbox ${lightboxOpen ? "show" : ""}`}
          onClick={(e) => {
            if (e.target === e.currentTarget) setLightboxOpen(false);
          }}
        >
          <img src={news.imageUrl} alt={news.title} />
          <button
            type="button"
            className="news-lightbox-close"
            aria-label="Đóng"
            onClick={() => setLightboxOpen(false)}
          >
            ✕
          </button>
        </div>
      ) : null}

      <Footer />
    </>
  );
}
