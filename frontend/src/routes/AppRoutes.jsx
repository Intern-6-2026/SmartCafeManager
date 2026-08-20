import { BrowserRouter, Routes, Route } from "react-router-dom";
import ClientMenu from "../pages/client-menu/client-menu";
import Login from "../pages/auth/Login";
import Register from "../pages/auth/Register";
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
import AdminItemList from "../pages/admin-items/AdminItemList";
import AdminItemForm from "../pages/admin-items/AdminItemForm";
import RequireRole from "../components/RequireRole";

export default function AppRoutes() {
    return (
        <BrowserRouter>
            <Routes>

                <Route path="/" element={<Login />} />

                <Route path="/register" element={<Register />} />

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

                <Route path="/news" element={<NewsList />} />
                <Route path="/news/:id" element={<NewsDetail />} />

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

                <Route
                    path="/admin/items"
                    element={
                        <RequireRole roles={["ADMIN", "STAFF"]}>
                            <AdminItemList />
                        </RequireRole>
                    }
                />
                <Route
                    path="/admin/items/new"
                    element={
                        <RequireRole roles={["ADMIN"]}>
                            <AdminItemForm />
                        </RequireRole>
                    }
                />
                <Route
                    path="/admin/items/:id/edit"
                    element={
                        <RequireRole roles={["ADMIN"]}>
                            <AdminItemForm />
                        </RequireRole>
                    }
                />
                
                <Route path="/payment-success" element={<PaymentSuccess />} />
            </Routes>
        </BrowserRouter>
    );
}
