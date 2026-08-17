import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Header from "../../components/header";
import Footer from "../../components/footer";
import { getNewsList, getApiErrorMessage } from "../../services/apiService";
import { canManageNews, formatNewsDate } from "../../utils/newsHelpers";
import "../../styles/news.css";

const PAGE_SIZE = 6;

export default function NewsList() {
  const [page, setPage] = useState(0);
  const [items, setItems] = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const manageNews = canManageNews();

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError("");

    getNewsList(page, PAGE_SIZE)
      .then((res) => {
        if (cancelled) return;
        const data = res.data || {};
        setItems(data.content || []);
        setTotalPages(Math.max(1, data.totalPages || 1));
        setTotalElements(data.totalElements ?? (data.content || []).length);
      })
      .catch((err) => {
        if (cancelled) return;
        setItems([]);
        setError(getApiErrorMessage(err, "Không tải được danh sách tin tức."));
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [page]);

  const goTo = (p) => {
    setPage(p);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  return (
    <>
      <Header />
      <main className="news-page">
        <div className="wrap">
          <div className="page-head">
            <div>
              <h1 className="page-title">Tin tức NEOCAFÉ</h1>
              <p className="page-sub">
                Cập nhật chương trình, món mới và câu chuyện từ quán.
              </p>
            </div>
            <div className="page-head-actions">
              <span className="page-count">
                {loading ? "Đang tải…" : `${totalElements} bài viết`}
              </span>
              <Link to="/home" className="news-btn news-btn-ghost">
                Về trang chủ
              </Link>
              {manageNews && (
                <Link to="/admin/news" className="news-btn news-btn-primary">
                  Quản lý tin tức
                </Link>
              )}
            </div>
          </div>

          {loading && <div className="news-loading">Đang tải tin tức…</div>}

          {!loading && error && <div className="news-error">{error}</div>}

          {!loading && !error && items.length === 0 && (
            <div className="news-empty">Chưa có tin tức nào được đăng.</div>
          )}

          {!loading && !error && items.length > 0 && (
            <>
              <div className="news-grid">
                {items.map((item) => (
                  <Link
                    key={item.newsId}
                    to={`/news/${item.newsId}`}
                    className="news-card"
                  >
                    <div className="news-card-media">
                      {item.imageUrl ? (
                        <img src={item.imageUrl} alt={item.title} />
                      ) : null}
                    </div>
                    <div className="news-card-body">
                      <div className="news-card-time">{formatNewsDate(item.createdAt)}</div>
                      <h2 className="news-card-title">{item.title}</h2>
                      <p className="news-card-summary">
                        {item.summary || "Xem chi tiết bài viết."}
                      </p>
                      <span className="news-card-more">Đọc tiếp →</span>
                    </div>
                  </Link>
                ))}
              </div>

              {totalPages > 1 && (
                <div className="news-pagination">
                  <button
                    type="button"
                    className="news-pg-btn"
                    disabled={page === 0}
                    onClick={() => goTo(page - 1)}
                  >
                    ← Trước
                  </button>
                  <div className="news-pg-nums">
                    {Array.from({ length: totalPages }, (_, i) => (
                      <button
                        key={i}
                        type="button"
                        className={`news-pg-num ${page === i ? "active" : ""}`}
                        onClick={() => goTo(i)}
                      >
                        {i + 1}
                      </button>
                    ))}
                  </div>
                  <button
                    type="button"
                    className="news-pg-btn"
                    disabled={page >= totalPages - 1}
                    onClick={() => goTo(page + 1)}
                  >
                    Sau →
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </main>
      <Footer />
    </>
  );
}
