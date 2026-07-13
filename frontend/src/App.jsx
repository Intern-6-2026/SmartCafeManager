import ClientMenu from "./pages/client-menu";
import { BrowserRouter, Routes, Route } from "react-router-dom";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/menu/table/:tableId" element={<ClientMenu/>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;