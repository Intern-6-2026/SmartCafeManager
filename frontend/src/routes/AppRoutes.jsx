import { BrowserRouter, Routes, Route } from "react-router-dom";
import ClientMenu from "../pages/client-menu/client-menu";
import Login from "../pages/auth/Login";
import ForgotPassword from "../pages/auth/ForgotPassword";
import Otp from "../pages/auth/Otp";
import NewPassword from "../pages/auth/NewPassword";
import Header from "../components/header";
import Body from "../components/body";
import Footer from "../components/footer";
import Profile from "../pages/profile/Profile";
import EditProfile from "../pages/profile/EditProfile";
import ChangePassword from "../pages/profile/ChangePassword";
import PaymentSuccess from "../pages/PaymentSuccess/paymentSuccess";
import NewsList from "../pages/news/NewsList";
import NewsDetail from "../pages/news/NewsDetail";
import AdminNewsList from "../pages/news/AdminNewsList";
import AdminNewsForm from "../pages/news/AdminNewsForm";
import AdminNewsDetail from "../pages/news/AdminNewsDetail";
import InvoiceManagement from "../pages/InvoiceManagement/InvoiceManagement";
import RevenueDashboard from "../pages/RevenueDashboard/RevenueDashboard";
import RequireRole from "../components/RequireRole";
import SaleManager from "../pages/sale-manager/saleManager"
import FeedbackManager from "../pages/feedback-manager/feedbackManager";

export default function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />

        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/otp" element={<Otp />} />
        <Route path="/new-password" element={<NewPassword />} />

        <Route path="/profile" element={<Profile />} />
        <Route path="/edit-profile" element={<EditProfile />} />
        <Route path="/change-password" element={<ChangePassword />} />

        {/* Trang chủ dành cho Khách hàng / Chung */}
        <Route
          path="/home"
          element={
            <>
              <Header />
              <Body />
              <Footer />
            </>
          }
        />

        <Route path="/news" element={<NewsList />} />
        <Route path="/news/:id" element={<NewsDetail />} />

        {/* Quản lý Tin tức (ADMIN) */}
        <Route
          path="/admin/news"
          element={
            <RequireRole roles={["ADMIN"]}>
              <AdminNewsList />
            </RequireRole>
          }
        />
        <Route
          path="/admin/news/new"
          element={
            <RequireRole roles={["ADMIN"]}>
              <AdminNewsForm />
            </RequireRole>
          }
        />
        <Route
          path="/admin/news/:id/edit"
          element={
            <RequireRole roles={["ADMIN"]}>
              <AdminNewsForm />
            </RequireRole>
          }
        />
        <Route
          path="/admin/news/:id"
          element={
            <RequireRole roles={["ADMIN"]}>
              <AdminNewsDetail />
            </RequireRole>
          }
        />

        {/* Quản lý hóa đơn (Dành cho ADMIN và STAFF) */}
        <Route
          path="/admin/invoices"
          element={
            <RequireRole roles={["ADMIN", "STAFF"]}>
              <InvoiceManagement />
            </RequireRole>
          }
        />

        {/* Thống kê thu nhập (Dành cho ADMIN) */}
        <Route
          path="/admin/revenue"
          element={
            <RequireRole roles={["ADMIN"]}>
              <RevenueDashboard />
            </RequireRole>
          }
        />

        <Route
            path="/menu"
            element={<ClientMenu />}
        />
        
        <Route 
            path="/payment-success" 
            element={<PaymentSuccess />} 
        />

        <Route 
            path="/sale-manager" 
            element={<SaleManager />} 
        />

        <Route 
            path="/feedback-manager" 
            element={<FeedbackManager />} 
          />
        </Routes>
    </BrowserRouter>
  );
}
