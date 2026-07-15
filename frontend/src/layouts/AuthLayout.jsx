export default function AuthLayout({
    title,
    subtitle,
    image,
    children,
}) {
    return (
        <div className="min-h-screen bg-[#F7F2ED] flex items-center justify-center p-6">

            <div className="bg-white w-full max-w-6xl rounded-[28px] overflow-hidden shadow-2xl grid lg:grid-cols-2">

                {/* Left */}

                <div className="bg-[#E7D6C4] flex flex-col justify-center items-center p-10">

                    <img
                        src="/images/logo.jpg"
                        alt="SmartCafe Logo"
                        className="w-64 h-64 object-contain mix-blend-multiply"
                    />

                    <img
                        src={image}
                        className="w-[420px]"
                        alt=""
                    />

                </div>

                {/* Right */}

                <div className="flex justify-center items-center">

                    <div className="w-full max-w-md">

                        <h1 className="text-4xl font-bold text-center text-[#5D4037]">
                            {title}
                        </h1>

                        <p className="text-center text-gray-500 mt-3 mb-10">
                            {subtitle}
                        </p>

                        {children}

                    </div>

                </div>

            </div>

        </div>
    );
}