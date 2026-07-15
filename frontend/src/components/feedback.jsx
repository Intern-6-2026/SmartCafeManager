import React, { useEffect, useState } from "react";
import "../styles/feedback.css";

/* ================= Modal "Phản Hồi" =================
   Hiện lên khi khách nhấp nút "Phản hồi" trong phần đơn hàng.
   Props:
   - open     : boolean — hiển thị modal hay không
   - onSubmit : ({ hoten, email, noidung, image }) => void — bấm "Gửi"
   - onClose  : () => void — bấm "Đóng" / overlay / phím Esc
====================================================== */

const EMPTY = { hoten: "", email: "", noidung: "", image: null };

function FeedbackModal({ open, onSubmit, onClose }) {
    const [form, setForm] = useState(EMPTY);

    // Reset form mỗi lần mở lại
    useEffect(() => {
        if (open) setForm(EMPTY);
    }, [open]);

    // Đóng bằng phím Esc
    useEffect(() => {
        if (!open) return;
        const onKey = (e) => e.key === "Escape" && onClose();
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [open, onClose]);

    if (!open) return null;

    const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

    const setImage = (e) => {
        const file = e.target.files?.[0] || null;
        setForm((f) => ({ ...f, image: file }));
    };

    const handleSubmit = () => {
        onSubmit(form);
    };

    return (
        <div
        className="modal-overlay feedback show"
        onClick={(e) => e.target === e.currentTarget && onClose()}
        >
        <div className="modal-box" role="dialog" aria-modal="true" aria-labelledby="feedback-title">
            <div className="modal-title" id="feedback-title">Phản Hồi</div>

            <div className="modal-field">
            <label className="modal-label" htmlFor="feedback-hoten">Họ và tên</label>
            <input
                type="text"
                id="feedback-hoten"
                className="modal-input"
                value={form.hoten}
                autoFocus
                onChange={set("hoten")}
            />
            </div>

            <div className="modal-field">
            <label className="modal-label" htmlFor="feedback-email">Email</label>
            <input
                type="email"
                id="feedback-email"
                className="modal-input"
                value={form.email}
                onChange={set("email")}
            />
            </div>

            <div className="modal-field">
            <label className="modal-label" htmlFor="feedback-noidung">Phản hồi</label>
            <textarea
                id="feedback-noidung"
                className="modal-textarea"
                value={form.noidung}
                onChange={set("noidung")}
            />
            </div>

            {/* Thêm ảnh (.jpg, .png) theo mockup */}
            <div className="modal-field upload-field">
            <span className="modal-label">Thêm ảnh</span>
            <label className="upload-btn" htmlFor="feedback-anh" title="Tải ảnh lên">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <path d="M12 16V5m0 0l-4 4m4-4l4 4" stroke="currentColor" strokeWidth="2"
                    strokeLinecap="round" strokeLinejoin="round" />
                <path d="M5 19h14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
                </svg>
            </label>
            <input
                type="file"
                id="feedback-anh"
                className="upload-input"
                accept=".jpg,.jpeg,.png"
                onChange={setImage}
            />
            <span className="upload-hint">
                {form.image ? form.image.name : ".jpg, .png"}
            </span>
            </div>

            <div className="modal-actions">
            <button className="btn-gui" onClick={handleSubmit}>Gửi</button>
            <button className="btn-dong" onClick={onClose}>Đóng</button>
            </div>
        </div>
        </div>
    );
}

export default FeedbackModal;