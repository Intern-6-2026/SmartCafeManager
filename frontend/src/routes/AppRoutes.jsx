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
import InvoiceManagement from "../pages/InvoiceManagement/InvoiceManagement";
import RevenueDashboard from "../pages/RevenueDashboard/RevenueDashboard";

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

        {/* Route trang chủ nhận QR code quét vào (VD: /home/1 hoặc /home/2) để lưu vào localStorage */}
        <Route
          path="/home/:tableId?"
          element={
            <>
              <Header />
              <Body />
              <Footer />
            </>
          }
        />

        {/* Route menu sạch hoàn toàn, chỉ đọc localStorage ra xài */}
        <Route path="/menu" element={<ClientMenu />} />

        <Route path="/payment-success" element={<PaymentSuccess />} />

        {/* --- CÁC ROUTE CHO ADMIN / QUẢN LÝ --- */}
        <Route path="/admin/invoices" element={<InvoiceManagement />} />
        <Route path="/admin/revenue" element={<RevenueDashboard />} />
      </Routes>
    </BrowserRouter>
  );
}
