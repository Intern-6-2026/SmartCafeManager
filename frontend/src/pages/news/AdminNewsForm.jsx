import React, { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import Header from "../../components/header";
import Footer from "../../components/footer";
import {
  createNews,
  updateNews,
  getAdminNewsById,
  getApiErrorMessage,
} from "../../services/apiService";
import { canEditOrDeleteNews, isAdminRole } from "../../utils/newsHelpers";
import "../../styles/news.css";

const emptyForm = {
  title: "",
  summary: "",
  content: "",
};

export default function AdminNewsForm() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const [form, setForm] = useState(emptyForm);
  const [image, setImage] = useState(null);
  const [preview, setPreview] = useState("");
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isEdit) return undefined;
    let cancelled = false;
    setLoading(true);
    getAdminNewsById(id)
      .then((res) => {
        if (cancelled) return;
        const n = res.data || {};
        if (!canEditOrDeleteNews(n.authorUsername) && !isAdminRole()) {
          setError("Bạn không có quyền sửa bài viết này (chỉ tác giả hoặc admin).");
          return;
        }
        setForm({
          title: n.title || "",
          summary: n.summary || "",
          content: n.content || "",
        });
        setPreview(n.imageUrl || "");
      })
      .catch((err) => {
        if (!cancelled) {
          setError(getApiErrorMessage(err, "Không tải được bài viết."));
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id, isEdit]);

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const onFile = (e) => {
    const file = e.target.files?.[0];
    setImage(file || null);
    if (file) {
      setPreview(URL.createObjectURL(file));
    }
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!form.title.trim() || !form.content.trim()) {
      setError("Vui lòng nhập tiêu đề và nội dung.");
      return;
    }
    setSaving(true);
    try {
      const payload = {
        title: form.title.trim(),
        summary: form.summary.trim(),
        content: form.content.trim(),
        image,
      };
      if (isEdit) {
        await updateNews(id, payload);
        navigate(`/admin/news/${id}`);
      } else {
        const res = await createNews(payload);
        const newId = res.data?.newsId;
        navigate(newId ? `/admin/news/${newId}` : "/admin/news");
      }
    } catch (err) {
      setError(getApiErrorMessage(err, "Lưu tin tức thất bại."));
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <Header />
      <main className="news-page">
        <div className="wrap">
          <Link to="/admin/news" className="news-detail-back">
            ← Quay lại danh sách
          </Link>

          <div className="page-head">
            <h1 className="page-title">
              {isEdit ? "Sửa tin tức" : "Tạo tin mới"}
            </h1>
          </div>

          {loading && <div className="news-loading">Đang tải…</div>}

          {!loading && error && isEdit && !form.title && (
            <div className="news-error">
              {error}{" "}
              <Link to="/admin/news" className="news-card-more">
                Quay lại danh sách
              </Link>
            </div>
          )}

          {!loading && !(error && isEdit && !form.title) && (
            <form className="news-form" onSubmit={onSubmit}>
              {error && <div className="news-error">{error}</div>}

              <label className="news-form-field">
                <span>Tiêu đề *</span>
                <input
                  name="title"
                  value={form.title}
                  onChange={onChange}
                  maxLength={255}
                  required
                />
              </label>

              <label className="news-form-field">
                <span>Tóm tắt</span>
                <textarea
                  name="summary"
                  value={form.summary}
                  onChange={onChange}
                  rows={3}
                />
              </label>

              <label className="news-form-field">
                <span>Nội dung *</span>
                <textarea
                  name="content"
                  value={form.content}
                  onChange={onChange}
                  rows={10}
                  required
                />
              </label>

              <label className="news-form-field">
                <span>Hình ảnh {isEdit ? "(để trống nếu giữ ảnh cũ)" : ""}</span>
                <input type="file" accept="image/*" onChange={onFile} />
              </label>

              {preview ? (
                <div className="news-form-preview">
                  <img src={preview} alt="Xem trước" />
                </div>
              ) : null}

              <div className="news-form-actions">
                <button
                  type="button"
                  className="news-btn news-btn-ghost"
                  onClick={() => navigate("/admin/news")}
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="news-btn news-btn-primary"
                  disabled={saving}
                >
                  {saving ? "Đang lưu…" : isEdit ? "Cập nhật" : "Tạo bài viết"}
                </button>
              </div>
            </form>
          )}
        </div>
      </main>
      <Footer />
    </>
  );
}
