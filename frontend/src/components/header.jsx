import React from "react";
import { Menu } from "lucide-react";
import logo from "../assets/logo.svg";

function Header() {
  return (
    <nav className="w-full bg-[#D2A97B] p-4 flex items-center justify-between shadow-md">
      <div className="flex items-center gap-2">
        <img src={logo} alt="Logo" className="w-10 h-10 object-contain" />
        <div className="text-[20px] font-['Inter']">
          <span className="font-bold text-[#000]">NEO</span>
          <span className="font-normal text-[#000]">CAFÉ</span>
        </div>
      </div>
      <button className="text-[#000]">
        <Menu size={28} />
      </button>
    </nav>
  );
}

export default Header;
