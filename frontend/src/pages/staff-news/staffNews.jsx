import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "../../styles/staff-news.css";
import NewsEditorModal from "../../components/newsEditorModal";
import {
  getAdminNewsList,
  getNewsList,
  getStaffNews,
  getNewsById,
  createNews,
  updateNews,
  deleteNews,
  changeNewsStatus,
  getApiErrorMessage,
} from "../../services/apiService";
import { ToastService } from "../../services/toastService";
import MenuButton from "../../components/menu-button";
const PAGE_SIZE = 10;

/* Nhãn + màu cho 4 trạng thái bài viết */
const STATUS_LABEL = {
  PUBLISHED: "Đã đăng",
  PENDING: "Chờ duyệt",
  REJECTED: "Từ chối",
};

/* Đọc role người đang đăng nhập từ localStorage */
const getRole = () => (localStorage.getItem("roleName") || "").toUpperCase();

/* Định dạng ISO datetime -> "dd/mm/yyyy HH:MM" (giờ địa phương) */
const formatDateTime = (iso) => {
  if (!iso) return "—";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return "—";
  const p = (n) => String(n).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()} ${p(d.getHours())}:${p(d.getMinutes())}`;
};

function StaffNewsManager() {
  const isAdmin = getRole() === "ADMIN";
  const navigate = useNavigate();
  const [news, setNews] = useState([]);
  const [myNewsIds, setMyNewsIds] = useState(() => new Set()); // newsId do staff hiện tại tạo
  const [page, setPage] = useState(0); // API dùng index từ 0
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [busyId, setBusyId] = useState(null);

  const [keyword, setKeyword] = useState("");
  const [statusFilter, setStatusFilter] = useState("");

  const [editorOpen, setEditorOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [viewersOf, setViewersOf] = useState(null);

  const notify = (msg, type = "info") => {
    const text = String(msg);
    if (type === "success") ToastService.success(text);
    else if (type === "error") ToastService.error(text);
    else ToastService.info(text);
  };

  /* Hiển thị TẤT CẢ tin (getNewsList), nhưng chỉ thao tác được trên tin
     do chính staff hiện tại tạo. Đối chiếu bằng getStaffNews (/news/my-news).
     Admin thì thấy tất cả và thao tác được hết. */
  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const res = isAdmin
        ? await getAdminNewsList(page, PAGE_SIZE)
        : await getNewsList(page, PAGE_SIZE);
      const data = res.data || {};
      const list = Array.isArray(data) ? data : data.content || [];
      setNews(list);
      setTotalPages(Math.max(1, data.totalPages || 1));
      setTotalElements(data.totalElements ?? list.length);

      // Staff: nạp danh sách tin của mình để biết tin nào được phép sửa/xoá
      if (!isAdmin) {
        try {
          const mineRes = await getStaffNews();
          const mineData = mineRes.data || {};
          const mineList = Array.isArray(mineData) ? mineData : mineData.content || [];
          setMyNewsIds(new Set(mineList.map((n) => n.newsId)));
        } catch {
          setMyNewsIds(new Set()); // không lấy được thì coi như không sở hữu tin nào
        }
      }
    } catch (err) {
      setNews([]);
      notify(err, "error");
    } finally {
      setLoading(false);
    }
  }, [isAdmin, page]);

  useEffect(() => {
    load();
  }, [load]);

  /* Lọc phía client trên trang hiện tại (từ khoá + trạng thái) */
  const visible = useMemo(() => {
    return news.filter((n) => {
      const okKw = !keyword || (n.title || "").toLowerCase().includes(keyword.toLowerCase());
      const okStatus = !statusFilter || n.status === statusFilter;
      return okKw && okStatus;
    });
  }, [news, keyword, statusFilter]);

  const openAdd = () => { setEditing(null); setEditorOpen(true); };
  /* Mở modal sửa: fetch nội dung đầy đủ bằng getNewsById (danh sách thường chỉ có summary) */
  const openEdit = async (item) => {
    setBusyId(item.newsId);
    try {
      const res = await getNewsById(item.newsId);
      setEditing(res.data || item); // dùng dữ liệu đầy đủ từ API
    } catch (err) {
      notify(getApiErrorMessage(err, "Không tải được nội dung bài viết."), "error");
      setEditing(item); // fallback: dùng dữ liệu đang có trên bảng
    } finally {
      setBusyId(null);
      setEditorOpen(true);
    }
  };

  /* Bấm vào ảnh/tiêu đề -> mở trang đọc bài (bất kể của ai) */
  const openReader = (id) => navigate(`/news/${id}`);

  const saveNews = async (data) => {
    try {
      const payload = {
        title: data.title,
        summary: data.summary || "",
        content: data.content,
        image: data.imageFile || null, // file ảnh mới (nếu có)
      };
      if (data.newsId) {
        await updateNews(data.newsId, payload);
      } else {
        await createNews(payload);
      }
      setEditorOpen(false);
      await load();
      notify("Chỉnh tin sửa thành công", "success")
    } catch (err) {
      notify(err, "error");
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Bạn chắc chắn muốn xóa tin tức này?")) return;
    setBusyId(id);
    try {
      await deleteNews(id);
      await load();
      notify("Xóa tin thành công", "sucess")
    } catch (err) {
      notify(err, "error");
    } finally {
      setBusyId(null);
      setEditorOpen(false);
    }
  };

  /* Duyệt / từ chối — chỉ admin */
  const handleStatus = async (id, status) => {
    setBusyId(id);
    try {
      await changeNewsStatus(id, status);
      await load();
      notify("Duyệt tin thành công", "sucess")
    } catch (err) {
      notify(err, "error");
    } finally {
      setBusyId(null);
    }
  };

  return (
    <div className="news-manager">
      <div className="topbar">
        <div className="brand">
          <div className="brand-mark">N</div>
          <div className="brand-name">NEOCAFÉ</div>
        </div>
        <div className="topbar-right">
          <span>Tin tức {isAdmin ? "· ADMIN" : "STAFF"}</span>
        </div>
        <MenuButton>

        </MenuButton>
      </div>

      <div className="wrap">
        <div className="news-toolbar">
          <div className="filter-left">
            <input
              className="news-search"
              placeholder="Tìm theo tiêu đề..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
            {/* <select
              className="news-status-filter"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">Tất cả trạng thái</option>
              <option value="PUBLISHED">Đã đăng</option>
              <option value="PENDING">Chờ duyệt</option>
              <option value="REJECTED">Từ chối</option>
            </select> */}
          </div>
          <button className="news-add-btn" onClick={openAdd}>＋ Thêm mới</button>
        </div>

        {error && <div className="news-error">{error}</div>}

        <div className="news-table-wrap">
          <table className="news-table">
            <thead>
              <tr>
                <th style={{ width: 48 }}>#</th>
                <th>Bài viết</th>
                <th style={{ width: 240 }}>Tóm tắt</th>
                <th style={{ width: 150 }}>Thời gian tạo</th>
                <th style={{ width: isAdmin ? 240 : 120 }}>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={5} className="news-empty">Đang tải...</td></tr>
              ) : visible.length === 0 ? (
                <tr><td colSpan={5} className="news-empty">Không có tin tức nào.</td></tr>
              ) : (
                visible.map((n, i) => (
                  <tr key={n.newsId}>
                    <td>{page * PAGE_SIZE + i + 1}</td>
                    <td>
                      <div
                        className="title-cell clickable"
                        onClick={() => openReader(n.newsId)}
                        role="button"
                        tabIndex={0}
                        onKeyDown={(e) => (e.key === "Enter" || e.key === " ") && openReader(n.newsId)}
                        title="Bấm để đọc bài viết"
                      >
                        {n.imageUrl && <img className="title-thumb" src={n.imageUrl} alt="" />}
                        <span className="title-text">{n.title}</span>
                      </div>
                    </td>
                    <td className="summary-cell">{n.summary || "—"}</td>
                    <td className="time-cell">{formatDateTime(n.createdAt)}</td>
                    <td>
                      <div className="action-cell">
                        {/* Chỉ tin do staff hiện tại tạo (hoặc admin) mới thao tác được */}
                        {(isAdmin || myNewsIds.has(n.newsId)) ? (
                          <>
                            <button className="act-btn edit" onClick={() => openEdit(n)} title="Sửa" disabled={busyId === n.newsId}>✎</button>
                            <button className="act-btn del" onClick={() => handleDelete(n.newsId)} title="Xóa" disabled={busyId === n.newsId}>🗑</button>
                          </>
                        ) : (
                          <span className="act-none" title="Tin do người khác tạo">—</span>
                        )}

                        {/* Nút duyệt tin — CHỈ admin thấy */}
                        {isAdmin && n.status !== "PUBLISHED" && (
                          <button
                            className="act-btn approve"
                            onClick={() => handleStatus(n.newsId, "PUBLISHED")}
                            title="Duyệt (đăng bài)"
                            disabled={busyId === n.newsId}
                          >
                            ✓
                          </button>
                        )}
                        {isAdmin && n.status === "PENDING" && (
                          <button
                            className="act-btn reject"
                            onClick={() => handleStatus(n.newsId, "REJECTED")}
                            title="Từ chối"
                            disabled={busyId === n.newsId}
                          >
                            ✕
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="news-foot">
          <span className="news-count">
            Trang {page + 1} / {totalPages} · Tổng {totalElements}
          </span>
          {totalPages > 1 && (
            <div className="news-pager">
              <button className="pg-btn" disabled={page === 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>‹</button>
              {Array.from({ length: totalPages }).map((_, i) => (
                <button
                  key={i}
                  className={`pg-num ${page === i ? "active" : ""}`}
                  onClick={() => setPage(i)}
                >
                  {i + 1}
                </button>
              ))}
              <button 
                className="pg-btn" 
                disabled={page >= totalPages - 1} 
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              >
                ›
              </button>
            </div>
          )}
        </div>
      </div>

      <NewsEditorModal
        open={editorOpen}
        initial={editing}
        onSave={saveNews}
        onDelete={handleDelete}
        onClose={() => setEditorOpen(false)}
      />
    </div>
  );
}

export default StaffNewsManager;