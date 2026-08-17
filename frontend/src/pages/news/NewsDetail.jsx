import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Header from "../../components/header";
import Footer from "../../components/footer";
import { getNewsById, getApiErrorMessage } from "../../services/apiService";
import {
  canManageNews,
  canEditOrDeleteNews,
  formatNewsDate,
} from "../../utils/newsHelpers";
import "../../styles/news.css";

export default function NewsDetail() {
  const { id } = useParams();
  const [news, setNews] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [lightboxOpen, setLightboxOpen] = useState(false);
  const manageNews = canManageNews();

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");
    setNews(null);

    getNewsById(id)
      .then((res) => {
        if (!cancelled) setNews(res.data);
      })
      .catch((err) => {
        if (!cancelled) {
          setError(getApiErrorMessage(err, "Không tìm thấy bài viết."));
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [id]);

  useEffect(() => {
    if (!lightboxOpen) return undefined;
    const onKey = (e) => {
      if (e.key === "Escape") setLightboxOpen(false);
    };
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [lightboxOpen]);

  return (
    <>
      <Header />
      <main className="news-page">
        <div className="wrap">
          <div className="page-head" style={{ marginBottom: 12 }}>
            <Link to="/news" className="news-detail-back" style={{ marginBottom: 0 }}>
              ← Quay lại tin tức
            </Link>
            {manageNews && (
              <div className="page-head-actions">
                <Link to={`/admin/news/${id}`} className="news-btn news-btn-ghost">
                  Xem bản quản lý
                </Link>
                {canEditOrDeleteNews() && (
                  <Link to={`/admin/news/${id}/edit`} className="news-btn news-btn-primary">
                    Sửa bài
                  </Link>
                )}
              </div>
            )}
          </div>

          {loading && <div className="news-loading">Đang tải bài viết…</div>}
          {!loading && error && <div className="news-error">{error}</div>}

          {!loading && !error && news && (
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
                <div className="news-detail-meta">{formatNewsDate(news.createdAt)}</div>
                <h1 className="news-detail-title">{news.title}</h1>
                {news.summary ? (
                  <p className="news-detail-summary">{news.summary}</p>
                ) : null}
                <div
                  className="news-detail-content"
                  dangerouslySetInnerHTML={{ __html: news.content || "" }}
                />
              </div>
            </article>
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
