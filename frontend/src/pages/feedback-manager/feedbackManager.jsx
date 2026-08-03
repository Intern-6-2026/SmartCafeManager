import React, { useMemo, useState } from "react";
import "../../styles/feedback-manager.css";
import ImageLightbox from "../../components/ImageLightbox";
import { SAMPLE_FEEDBACKS, PAGE_SIZE } from "../../data/feedbackData";

const initials = (name) =>
  name.split(" ").map((w) => w[0]).slice(-2).join("").toUpperCase();

function Stars({ n }) {
  return (
    <div className="rating">
      {[1, 2, 3, 4, 5].map((i) => (
        <span key={i} className={i <= n ? "" : "off"}>★</span>
      ))}
      <span className="rating-num">{n}/5</span>
    </div>
  );
}

function FeedbackManager() {
  const [page, setPage] = useState(1);
  const [sortKey, setSortKey] = useState("time"); // "time" | "rating"
  const [sortDir, setSortDir] = useState("desc"); // "asc" | "desc"
  const [filterItem, setFilterItem] = useState("");
  const [filterTable, setFilterTable] = useState("");
  const [lightbox, setLightbox] = useState(null);

  // Danh sách món / bàn cho dropdown lọc (không trùng)
  const itemOptions = useMemo(
    () => [...new Set(SAMPLE_FEEDBACKS.map((r) => r.item))].sort(),
    []
  );
  const tableOptions = useMemo(
    () => [...new Set(SAMPLE_FEEDBACKS.map((r) => r.table))].sort(),
    []
  );

  // Lọc -> sắp xếp
  const processed = useMemo(() => {
    const filtered = SAMPLE_FEEDBACKS.filter(
      (r) =>
        (!filterItem || r.item === filterItem) &&
        (!filterTable || r.table === filterTable)
    );
    const sorted = [...filtered].sort((a, b) => {
      const va = sortKey === "rating" ? a.rating : a.ts;
      const vb = sortKey === "rating" ? b.rating : b.ts;
      return sortDir === "asc" ? va - vb : vb - va;
    });
    return sorted;
  }, [filterItem, filterTable, sortKey, sortDir]);

  const totalPages = Math.max(1, Math.ceil(processed.length / PAGE_SIZE));
  const safePage = Math.min(page, totalPages);
  const rows = processed.slice((safePage - 1) * PAGE_SIZE, safePage * PAGE_SIZE);

  const toggleSort = (key) => {
    if (sortKey === key) {
      setSortDir((d) => (d === "asc" ? "desc" : "asc"));
    } else {
      setSortKey(key);
      setSortDir("desc");
    }
    setPage(1);
  };

  const sortInd = (key) => (sortKey === key ? (sortDir === "asc" ? "▲" : "▼") : "");

  const resetFilters = () => {
    setFilterItem("");
    setFilterTable("");
    setPage(1);
  };

  return (
    <div className="feedback-manager">
      <div className="topbar">
        <div className="brand">
          <div className="brand-mark">N</div>
          <div className="brand-name">NEOCAFÉ</div>
        </div>
        <div className="topbar-right">
          <span>Quản lý</span>
          <div className="staff"><div className="staff-avatar">QL</div>Minh Quân</div>
        </div>
      </div>

      <div className="wrap">
        <div className="page-head">
          <div className="page-title">Quản lý phản hồi</div>
          <div className="page-count">{processed.length} phản hồi</div>
        </div>

        <div className="filter-bar">
          <div className="filter-field">
            <label htmlFor="filterItem">Lọc theo món</label>
            <select
              id="filterItem"
              value={filterItem}
              onChange={(e) => { setFilterItem(e.target.value); setPage(1); }}
            >
              <option value="">Tất cả món</option>
              {itemOptions.map((v) => <option key={v} value={v}>{v}</option>)}
            </select>
          </div>
          <div className="filter-field">
            <label htmlFor="filterTable">Lọc theo bàn</label>
            <select
              id="filterTable"
              value={filterTable}
              onChange={(e) => { setFilterTable(e.target.value); setPage(1); }}
            >
              <option value="">Tất cả bàn</option>
              {tableOptions.map((v) => <option key={v} value={v}>{v}</option>)}
            </select>
          </div>
          <button className="filter-clear" onClick={resetFilters}>Xóa lọc</button>
        </div>

        <div className="table-scroll">
          <table className="fb-table">
            <thead>
              <tr>
                <th style={{ width: "52px" }}>ID</th>
                <th className="sortable" onClick={() => toggleSort("time")}>
                  Gửi lúc <span className="sort-ind">{sortInd("time")}</span>
                </th>
                <th>Người tạo</th>
                <th>Email</th>
                <th>Bàn</th>
                <th>Món</th>
                <th className="sortable" onClick={() => toggleSort("rating")}>
                  Rating <span className="sort-ind">{sortInd("rating")}</span>
                </th>
                <th>Nội dung</th>
                <th>Hình ảnh</th>
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr>
                  <td colSpan={9} className="fb-state">Không có phản hồi nào khớp bộ lọc.</td>
                </tr>
              ) : (
                rows.map((r) => (
                  <tr key={r.id}>
                    <td className="col-id">#{r.id}</td>
                    <td className="col-time">{r.time}</td>
                    <td className="col-name">{r.name}</td>
                    <td className="col-email">{r.email}</td>
                    <td className="col-table">{r.table}</td>
                    <td className="col-item">{r.item}</td>
                    <td><Stars n={r.rating} /></td>
                    <td className="col-content">{r.content}</td>
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
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={safePage === 1}
            >
              ← Trước
            </button>
            <div className="pg-nums">
              {Array.from({ length: totalPages }).map((_, i) => (
                <button
                  key={i}
                  className={`pg-num ${safePage === i + 1 ? "active" : ""}`}
                  onClick={() => setPage(i + 1)}
                >
                  {i + 1}
                </button>
              ))}
            </div>
            <button
              className="pg-btn"
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={safePage === totalPages}
            >
              Sau →
            </button>
          </div>
        )}
      </div>

      <ImageLightbox src={lightbox} onClose={() => setLightbox(null)} />
    </div>
  );
}

export default FeedbackManager;