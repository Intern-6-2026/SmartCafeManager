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

export default function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>

                <Route path="/" element={<Login />} />

                <Route
                    path="/forgot-password"
                    element={<ForgotPassword />}
                />

                <Route
                    path="/otp"
                    element={<Otp />}
                />

                <Route
                    path="/new-password"
                    element={<NewPassword />}
                />

                <Route
                    path="/profile"
                    element={<Profile />}
                />

                <Route
                    path="/edit-profile"
                    element={<EditProfile />}
                />

                <Route
                    path="/change-password"
                    element={<ChangePassword />}
                />
                
                <Route
                    path="/home"
                    element={
                        <>
                            <Header/>
                            <Body/>
                            <Footer/>
                        </>
                    }
                />

                <Route
                    path="/menu/table/:tableId"
                    element={<ClientMenu />}
                />
                
                <Route path="/payment-success" element={<PaymentSuccess />} />
            </Routes>
        </BrowserRouter>
    );
}