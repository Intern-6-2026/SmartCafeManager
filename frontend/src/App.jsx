import ClientMenu from "./pages/client-menu";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Header from "./components/header";
import Body from "./components/body";
import Footer from "./components/footer";
function App() {
  return (
    <>
      <BrowserRouter>
        <Routes>
          {/* Route cho trang chủ */}
          <Route
            path="/"
            element={
              <>
                <Header />
                <Body />
                <Footer />
              </>
            }
          />

          {/* Route cho trang Menu */}
          <Route path="/menu/table/:tableId" element={<ClientMenu />} />
        </Routes>
      </BrowserRouter>
    </>
  );
}

export default App;
