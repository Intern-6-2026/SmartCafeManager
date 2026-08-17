import React, { useEffect, useRef, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import coffeeBeans from "../assets/coffee-beans.jpg";
import {
  getLatestItems,
  getBestSellerItems,
  addItemToCart,
  getNewsList,
} from "../services/apiService";
import { canManageNews, formatNewsDate } from "../utils/newsHelpers";
import "../styles/news.css";

function Body() {
  const { tableId: urlTableId } = useParams();
  const navigate = useNavigate();
  const newsTrackRef = useRef(null);
  const [latestItems, setLatestItems] = useState([]);
  const [bestSellerItems, setBestSellerItems] = useState([]);
  const [latestNews, setLatestNews] = useState([]);
  const [newsTotal, setNewsTotal] = useState(0);
  const [canScrollLeft, setCanScrollLeft] = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  const roleName = (localStorage.getItem("roleName") || "").toUpperCase();
  const isAdmin = roleName === "ADMIN";
  const isStaff = roleName === "STAFF";
  const manageNews = canManageNews();

  const updateNewsScrollState = () => {
    const el = newsTrackRef.current;
    if (!el) {
      setCanScrollLeft(false);
      setCanScrollRight(false);
      return;
    }
    const maxScroll = el.scrollWidth - el.clientWidth;
    setCanScrollLeft(el.scrollLeft > 4);
    setCanScrollRight(el.scrollLeft < maxScroll - 4);
  };

  const scrollNews = (dir) => {
    const el = newsTrackRef.current;
    if (!el) return;
    const step = Math.min(340, el.clientWidth * 0.85);
    el.scrollBy({ left: dir * step, behavior: "smooth" });
  };

  // --- BƯỚC 1: Bắt số bàn từ URL (nếu khách quét QR vào trang chủ) và lưu vào localStorage ---
  useEffect(() => {
    if (urlTableId) {
      localStorage.setItem("tableId", urlTableId);
    } else {
      localStorage.setItem("tableId", 1);
      console.log("Table: "+localStorage.getItem("tableId"));
    }
  }, [urlTableId]);

  // Lấy ra tableId đang lưu trong máy (mặc định là "1" nếu chưa quét QR)
  const currentTableId = localStorage.getItem("tableId") || "1";

  useEffect(() => {
    getLatestItems().then((res) => {
      setLatestItems(res.data || []);
    });
    getBestSellerItems().then((res) => {
      setBestSellerItems(res.data || []);
    });

    // Gọi API lấy tin tức công khai an toàn cho mọi role
    getNewsList(0, 50)
      .then((res) => {
        const list = res.data?.content || res.data || [];
        setLatestNews(list);
        setNewsTotal(res.data?.totalElements ?? list.length);
      })
      .catch(() => {
        setLatestNews([]);
        setNewsTotal(0);
      });
  }, []);

  useEffect(() => {
    const el = newsTrackRef.current;
    if (!el) return undefined;
    updateNewsScrollState();
    el.addEventListener("scroll", updateNewsScrollState, { passive: true });
    window.addEventListener("resize", updateNewsScrollState);
    return () => {
      el.removeEventListener("scroll", updateNewsScrollState);
      window.removeEventListener("resize", updateNewsScrollState);
    };
  }, [latestNews]);

  const handleItemClick = async (item) => {
    const itemId = item.itemId || item.id;
    const tableId = 1;

    try {
      await addItemToCart(tableId, itemId, 1, "");
    } catch (error) {
      console.error("Lỗi khi thêm vào giỏ hàng:", error);
    }
    window.scrollTo(0, 0);
    navigate(`/menu`);
  };

  return (
    <main className="w-full">
      {/* THANH ĐIỀU HƯỚNG NHANH CHO ADMIN / STAFF NẾU CẦN */}
      {(isAdmin || isStaff) && (
        <div className="bg-[#33261A] text-[#E7C9A1] px-6 py-2.5 flex justify-between items-center text-sm font-medium">
          <span>
            Xin chào, {isAdmin ? "Quản trị viên (Admin)" : "Nhân viên (Staff)"}
          </span>
          <div className="flex gap-3">
            {isAdmin && (
              <button
                onClick={() => navigate("/admin/revenue")}
                className="bg-[#E7C9A1] text-[#33261A] px-3 py-1 rounded-md text-xs font-bold hover:bg-white cursor-pointer"
              >
                📊 Xem Thống kê Thu nhập
              </button>
            )}
            <button
              onClick={() => navigate("/admin/invoices")}
              className="bg-[#E7C9A1] text-[#33261A] px-3 py-1 rounded-md text-xs font-bold hover:bg-white cursor-pointer"
            >
              📑 Quản lý Hóa đơn
            </button>
          </div>
        </div>
      )}

      {/* 1. Phần Hero */}
      <section className="relative w-full h-[300px] flex items-center">
        <img
          src={coffeeBeans}
          alt="Coffee Beans"
          className="absolute inset-0 w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-black/40"></div>
        <div className="relative z-10 px-6 text-white font-['Inter']">
          <p className="text-[16px] font-normal">
            Chào mừng bạn đến bàn {currentTableId},
          </p>
          <h1 className="text-[24px] font-bold my-2">
            Trải nghiệm cà phê thông minh
          </h1>
          <div className="flex flex-wrap gap-3 mt-2">
            <button
              onClick={() => navigate("/menu")}
              className="bg-white text-black px-6 py-2 rounded-full font-medium cursor-pointer hover:bg-gray-100"
            >
              Đặt món
            </button>
            <button
              onClick={() => navigate("/news")}
              className="bg-transparent text-white px-6 py-2 rounded-full font-medium cursor-pointer border border-white/80 hover:bg-white/15"
            >
              Xem tin tức
            </button>
          </div>
        </div>
      </section>

      {/* Tin tức trên dashboard */}
      <section className="home-news my-8">
        <div className="home-news-inner">
          <div className="home-news-head">
            <div>
              <h2>Tin tức NEOCAFÉ</h2>
              <p>
                Vuốt ngang để xem thêm
                {newsTotal > 0 ? ` · ${newsTotal} bài viết` : ""}.
              </p>
            </div>
            <div className="home-news-actions">
              {latestNews.length > 0 && (
                <div className="home-news-nav">
                  <button
                    type="button"
                    className="home-news-nav-btn"
                    aria-label="Lướt trái"
                    disabled={!canScrollLeft}
                    onClick={() => scrollNews(-1)}
                  >
                    ←
                  </button>
                  <button
                    type="button"
                    className="home-news-nav-btn"
                    aria-label="Lướt phải"
                    disabled={!canScrollRight}
                    onClick={() => scrollNews(1)}
                  >
                    →
                  </button>
                </div>
              )}
              <Link to="/news" className="news-btn news-btn-primary">
                Xem tất cả
              </Link>
              {manageNews && (
                <Link to="/admin/news" className="news-btn news-btn-ghost">
                  Quản lý
                </Link>
              )}
            </div>
          </div>

          {latestNews.length === 0 ? (
            <div className="home-news-empty">
              Chưa có tin tức công khai.
              {manageNews ? (
                <>
                  {" "}
                  <Link to="/admin/news/new" className="news-card-more">
                    Thêm bài viết đầu tiên →
                  </Link>
                </>
              ) : null}
            </div>
          ) : (
            <>
              <div className="home-news-carousel">
                <div
                  className="home-news-track"
                  ref={newsTrackRef}
                  onScroll={updateNewsScrollState}
                >
                  {latestNews.map((item) => (
                    <Link
                      key={item.newsId || item.id}
                      to={`/news/${item.newsId || item.id}`}
                      className="news-card home-news-card"
                    >
                      <div className="news-card-media">
                        {item.imageUrl ? (
                          <img src={item.imageUrl} alt={item.title} />
                        ) : null}
                      </div>
                      <div className="news-card-body">
                        <div className="news-card-time">
                          {formatNewsDate(item.createdAt)}
                        </div>
                        <h3 className="news-card-title">{item.title}</h3>
                        <p className="news-card-summary">
                          {item.summary || "Xem chi tiết bài viết."}
                        </p>
                        <span className="news-card-more">Đọc tiếp →</span>
                      </div>
                    </Link>
                  ))}
                  <Link to="/news" className="home-news-see-all-card">
                    <span className="home-news-see-all-title">
                      Xem tất cả tin tức
                    </span>
                    <span className="home-news-see-all-sub">
                      Mở trang danh sách đầy đủ
                    </span>
                    <span className="home-news-see-all-arrow">→</span>
                  </Link>
                </div>
              </div>
              <div className="home-news-footer">
                <Link to="/news" className="news-card-more">
                  Xem tất cả tin tức →
                </Link>
              </div>
            </>
          )}
        </div>
      </section>

      {/* 2. Phần Món mới nhất */}
      <section className="container mx-auto my-8 overflow-hidden rounded-lg">
        <div className="bg-[#EBE2CB] p-6 flex flex-col items-center">
          <h2 className="text-2xl font-bold mb-4">Top 4 món mới nhất</h2>
          <button
            onClick={() => navigate("/menu")}
            className="bg-[#5C4D3F] text-white px-6 py-2 rounded-full font-medium cursor-pointer"
          >
            Khám phá
          </button>
        </div>

        <div className="bg-white p-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {latestItems.map((item) => (
            <div
              key={item.itemId || item.id}
              className="flex flex-col items-center text-center"
            >
              <img
                src={
                  item.imageUrl && item.imageUrl.trim() !== ""
                    ? item.imageUrl
                    : "https://via.placeholder.com/150"
                }
                alt={item.itemName}
                onClick={() => handleItemClick(item)}
                className="w-40 h-40 object-cover rounded-2xl mb-4 bg-gray-200 cursor-pointer hover:opacity-80 transition-opacity"
                onError={(e) => {
                  e.target.src = "https://via.placeholder.com/150";
                }}
              />
              <h3
                onClick={() => handleItemClick(item)}
                className="font-bold text-lg cursor-pointer hover:text-[#A4B435] transition-colors"
              >
                {item.itemName}
              </h3>
              <p className="text-[#A4B435] font-bold">
                {item.price ? `${item.price.toLocaleString()} vnđ` : "Liên hệ"}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* 3. Phần Món bán chạy nhất */}
      <section className="container mx-auto my-8 overflow-hidden rounded-lg">
        <div className="bg-[#EBE2CB] p-6 flex flex-col items-center">
          <h2 className="text-2xl font-bold mb-4">
            Top những món bán chạy nhất
          </h2>
          <button
            onClick={() => navigate("/menu")}
            className="bg-[#5C4D3F] text-white px-6 py-2 rounded-full font-medium cursor-pointer"
          >
            Khám phá
          </button>
        </div>

        <div className="bg-white p-6 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {bestSellerItems.map((item) => (
            <div
              key={item.itemId || item.id}
              className="flex flex-col items-center text-center"
            >
              <img
                src={
                  item.imageUrl && item.imageUrl.trim() !== ""
                    ? item.imageUrl
                    : "https://via.placeholder.com/150"
                }
                alt={item.itemName}
                onClick={() => handleItemClick(item)}
                className="w-40 h-40 object-cover rounded-2xl mb-4 bg-gray-200 cursor-pointer hover:opacity-80 transition-opacity"
                onError={(e) => {
                  e.target.src = "https://via.placeholder.com/150";
                }}
              />
              <h3
                onClick={() => handleItemClick(item)}
                className="font-bold text-lg cursor-pointer hover:text-[#A4B435] transition-colors"
              >
                {item.itemName}
              </h3>
              <p className="text-[#A4B435] font-bold">
                {item.price ? `${item.price.toLocaleString()} vnđ` : "Liên hệ"}
              </p>
            </div>
          ))}
        </div>
      </section>

      {/* 4. Footer Email */}
      <section className="bg-[#EBE2CB] p-8 md:p-12 rounded-xl w-full max-w-lg mx-auto my-10 text-center">
        <h2 className="text-2xl font-bold text-[#4a3f33] mb-3">
          Đăng kí để nhận voucher khuyến mãi 15%!
        </h2>
        <p className="text-[#70665b] mb-6">
          Đăng kí thành viên để nhận voucher khuyến mãi 15% cho lần mua sắm tiếp
          theo
        </p>
        <div className="flex flex-col gap-4">
          <div className="flex items-center w-full bg-white rounded-lg px-4 py-3 border border-gray-300 shadow-sm focus-within:ring-2 focus-within:ring-[#4a3f33]">
            <input
              type="email"
              placeholder="Địa chỉ email"
              className="flex-grow bg-transparent outline-none text-gray-700 placeholder-gray-500 px-2"
            />
          </div>
          <button className="w-full bg-[#3E2723] text-white py-3 rounded-lg font-bold text-lg hover:bg-[#5D4037] transition-all duration-300 shadow-md cursor-pointer">
            Đăng kí
          </button>
          {/* NÚT ĐẶT MÓN NỔI (FLOATING BUTTON) LUÔN HIỆN KHI CUỘN TRANG */}
          <button
            onClick={() => navigate("/menu")}
            className="fixed bottom-6 right-6 z-50 bg-[#33261A] text-[#E7C9A1] px-5 py-3.5 rounded-full shadow-2xl flex items-center gap-2.5 font-bold text-sm hover:bg-[#4E3928] hover:scale-105 transition-all duration-300 border-2 border-[#E7C9A1] cursor-pointer"
            title="Đặt món ngay"
          >
            <span className="text-lg">☕</span>
            <span>Đặt món ngay</span>
          </button>
        </div>
      </section>
    </main>
  );
}

export default Body;
