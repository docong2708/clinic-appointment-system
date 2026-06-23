import { useMemo, useState } from 'react';
import {
  ArrowRight,
  BriefcaseMedical,
  Building2,
  CalendarCheck2,
  CalendarDays,
  CircleUserRound,
  ClipboardList,
  Clock3,
  FileText,
  HeartPulse,
  Hospital,
  LogIn,
  LogOut,
  Search,
  ShieldCheck,
  Stethoscope,
  UserRoundPlus,
  Users,
  X,
} from 'lucide-react';

const specialties = [
  { id: 'sp-1', label: 'Đặt khám theo bệnh viện', icon: Hospital },
  { id: 'sp-2', label: 'Đặt khám theo bác sĩ', icon: Stethoscope },
  { id: 'sp-3', label: 'Đặt khám theo chuyên khoa', icon: HeartPulse },
  { id: 'sp-4', label: 'Gói khám sức khỏe', icon: ShieldCheck },
  { id: 'sp-5', label: 'Khám doanh nghiệp', icon: BriefcaseMedical },
  { id: 'sp-6', label: 'Tư vấn từ xa', icon: ClipboardList },
];

const doctors = [
  {
    id: 'DOC-01',
    name: 'BS.CKII Lý Thị Mỹ Dung',
    specialty: 'Sản khoa',
    clinic: 'Phòng khám An Phúc - Quận 3',
    price: '320.000 đ',
    schedule: '08:00 - 11:30 | Thứ 2 - Thứ 6',
  },
  {
    id: 'DOC-02',
    name: 'BS. Nguyễn Minh Khang',
    specialty: 'Tim mạch',
    clinic: 'Bệnh viện Đa khoa Tâm Đức',
    price: '250.000 đ',
    schedule: '13:30 - 17:00 | Thứ 2 - Thứ 7',
  },
  {
    id: 'DOC-03',
    name: 'BS. Trần Thu Hà',
    specialty: 'Da liễu',
    clinic: 'Phòng khám Thanh Bình',
    price: '220.000 đ',
    schedule: '09:00 - 16:00 | Thứ 3 - Chủ nhật',
  },
  {
    id: 'DOC-04',
    name: 'BS. Phạm Mỹ Linh',
    specialty: 'Nhi khoa',
    clinic: 'Bệnh viện Nhi Sài Gòn',
    price: '280.000 đ',
    schedule: '07:30 - 11:00 | Thứ 2 - Thứ 7',
  },
];

const initialUsers = [
  {
    id: 'U-001',
    fullName: 'Nguyễn Thảo Vy',
    email: 'patient@clinic.vn',
    phone: '0901234567',
    password: '123456',
    role: 'PATIENT',
  },
  {
    id: 'U-002',
    fullName: 'BS. Nguyễn Minh Khang',
    email: 'doctor@clinic.vn',
    phone: '0907654321',
    password: '123456',
    role: 'DOCTOR',
  },
  {
    id: 'U-003',
    fullName: 'Quản trị hệ thống',
    email: 'admin@clinic.vn',
    phone: '0988888888',
    password: '123456',
    role: 'ADMIN',
  },
];

const initialAppointments = [
  {
    id: 'APT-1001',
    patientName: 'Nguyễn Thảo Vy',
    patientEmail: 'patient@clinic.vn',
    doctorId: 'DOC-01',
    doctorName: 'BS.CKII Lý Thị Mỹ Dung',
    specialty: 'Sản khoa',
    clinic: 'Phòng khám An Phúc - Quận 3',
    date: '2026-06-24',
    time: '08:30',
    status: 'Đã xác nhận',
    reason: 'Khám thai định kỳ',
  },
  {
    id: 'APT-1002',
    patientName: 'Lê Minh Châu',
    patientEmail: 'walkin@clinic.vn',
    doctorId: 'DOC-02',
    doctorName: 'BS. Nguyễn Minh Khang',
    specialty: 'Tim mạch',
    clinic: 'Bệnh viện Đa khoa Tâm Đức',
    date: '2026-06-24',
    time: '14:00',
    status: 'Chờ khám',
    reason: 'Tái khám tim mạch',
  },
  {
    id: 'APT-1003',
    patientName: 'Phạm Gia Hân',
    patientEmail: 'kid@clinic.vn',
    doctorId: 'DOC-04',
    doctorName: 'BS. Phạm Mỹ Linh',
    specialty: 'Nhi khoa',
    clinic: 'Bệnh viện Nhi Sài Gòn',
    date: '2026-06-25',
    time: '09:00',
    status: 'Đã xác nhận',
    reason: 'Khám sốt và ho',
  },
];

const roleLabel = {
  PATIENT: 'Bệnh nhân',
  DOCTOR: 'Bác sĩ',
  ADMIN: 'Quản trị viên',
};

function formatDate(date) {
  const [year, month, day] = date.split('-');
  return `${day}/${month}/${year}`;
}

function nextAppointmentId(items) {
  return `APT-${1000 + items.length + 1}`;
}

function App() {
  const [users, setUsers] = useState(initialUsers);
  const [appointments, setAppointments] = useState(initialAppointments);
  const [currentUser, setCurrentUser] = useState(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [showQuickBooking, setShowQuickBooking] = useState(false);
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [authMode, setAuthMode] = useState('login');
  const [authMessage, setAuthMessage] = useState('');
  const [roleTab, setRoleTab] = useState('overview');
  const [loginForm, setLoginForm] = useState({
    email: 'patient@clinic.vn',
    password: '123456',
  });
  const [registerForm, setRegisterForm] = useState({
    fullName: '',
    email: '',
    phone: '',
    password: '',
    role: 'PATIENT',
  });
  const [quickBookingForm, setQuickBookingForm] = useState({
    fullName: '',
    phone: '',
    facility: '',
    need: '',
  });
  const [bookingForm, setBookingForm] = useState({
    doctorId: doctors[0].id,
    date: '2026-06-26',
    time: '08:00',
    reason: '',
  });
  const [quickBookingMessage, setQuickBookingMessage] = useState('');
  const [bookingMessage, setBookingMessage] = useState('');

  const filteredDoctors = useMemo(() => {
    const keyword = searchKeyword.trim().toLowerCase();
    if (!keyword) {
      return doctors;
    }
    return doctors.filter(
      (doctor) =>
        doctor.name.toLowerCase().includes(keyword) ||
        doctor.specialty.toLowerCase().includes(keyword) ||
        doctor.clinic.toLowerCase().includes(keyword)
    );
  }, [searchKeyword]);

  const patientAppointments = useMemo(() => {
    if (!currentUser) {
      return [];
    }
    return appointments.filter((item) => item.patientEmail === currentUser.email);
  }, [appointments, currentUser]);

  const doctorAppointments = useMemo(() => {
    if (!currentUser) {
      return [];
    }
    return appointments.filter((item) => item.doctorName === currentUser.fullName);
  }, [appointments, currentUser]);

  const selectedDoctor = useMemo(
    () => doctors.find((doctor) => doctor.id === bookingForm.doctorId) ?? doctors[0],
    [bookingForm.doctorId]
  );

  const openAuth = (mode) => {
    setAuthMode(mode);
    setAuthMessage('');
    setShowAuthModal(true);
  };

  const handleLogin = (event) => {
    event.preventDefault();
    const user = users.find(
      (item) =>
        item.email.toLowerCase() === loginForm.email.trim().toLowerCase() &&
        item.password === loginForm.password
    );
    if (!user) {
      setAuthMessage('Email hoặc mật khẩu chưa đúng. Vui lòng kiểm tra lại.');
      return;
    }
    setCurrentUser(user);
    setRoleTab('overview');
    setShowAuthModal(false);
  };

  const handleRegister = (event) => {
    event.preventDefault();
    const email = registerForm.email.trim().toLowerCase();
    if (users.some((user) => user.email.toLowerCase() === email)) {
      setAuthMessage('Email này đã tồn tại trong hệ thống.');
      return;
    }
    const newUser = {
      id: `U-${String(users.length + 1).padStart(3, '0')}`,
      fullName: registerForm.fullName.trim(),
      email,
      phone: registerForm.phone.trim(),
      password: registerForm.password,
      role: registerForm.role,
    };
    setUsers((current) => [...current, newUser]);
    setCurrentUser(newUser);
    setRoleTab('overview');
    setShowAuthModal(false);
    setRegisterForm({
      fullName: '',
      email: '',
      phone: '',
      password: '',
      role: 'PATIENT',
    });
  };

  const handleQuickBookingSubmit = (event) => {
    event.preventDefault();
    setQuickBookingMessage('Yêu cầu của bạn đã được gửi. Hệ thống sẽ liên hệ trong thời gian sớm nhất.');
    setQuickBookingForm({
      fullName: '',
      phone: '',
      facility: '',
      need: '',
    });
  };

  const handleBookingSubmit = (event) => {
    event.preventDefault();
    if (!currentUser) {
      setBookingMessage('Bạn cần đăng nhập trước khi đặt lịch khám.');
      return;
    }

    const appointment = {
      id: nextAppointmentId(appointments),
      patientName: currentUser.fullName,
      patientEmail: currentUser.email,
      doctorId: selectedDoctor.id,
      doctorName: selectedDoctor.name,
      specialty: selectedDoctor.specialty,
      clinic: selectedDoctor.clinic,
      date: bookingForm.date,
      time: bookingForm.time,
      status: 'Đã xác nhận',
      reason: bookingForm.reason.trim(),
    };

    setAppointments((current) => [appointment, ...current]);
    setBookingMessage(`Bạn đã đặt lịch với ${selectedDoctor.name} vào ${bookingForm.time}, ngày ${formatDate(bookingForm.date)}.`);
    setBookingForm((current) => ({
      ...current,
      reason: '',
    }));
    if (currentUser.role === 'PATIENT') {
      setRoleTab('appointments');
    }
  };

  const handleLogout = () => {
    setCurrentUser(null);
    setRoleTab('overview');
    setBookingMessage('');
  };

  const renderPublicHome = () => (
    <div className="site-page">
      <header className="site-header">
        <div className="container header-inner">
          <div className="brand">
            <div className="brand-icon">
              <HeartPulse size={20} />
            </div>
            <span>ClinicAppointment</span>
          </div>

          <nav className="main-nav">
            <a href="#home">Trang chủ</a>
            <a href="#doctors">Bác sĩ</a>
            <a href="#hospitals">Bệnh viện</a>
            <a href="#clinics">Phòng khám</a>
            <a href="#packages">Gói khám</a>
          </nav>

          <div className="header-actions">
            <button type="button" className="text-button" onClick={() => openAuth('login')}>
              Đăng nhập
            </button>
            <button type="button" className="primary-button small" onClick={() => openAuth('register')}>
              Đăng ký
            </button>
          </div>
        </div>
      </header>

      <section id="home" className="hero-section">
        <div className="container hero-content">
          <p className="hero-kicker">Kết nối bệnh nhân với bác sĩ và bệnh viện</p>
          <h1>Đặt lịch khám bệnh trực tuyến nhanh, dễ dùng và rõ ràng bằng tiếng Việt</h1>
          <p className="hero-subtitle">
            Tìm bác sĩ, bệnh viện, phòng khám phù hợp và gửi yêu cầu đặt khám chỉ trong vài bước.
          </p>

          <div className="hero-search">
            <input
              type="text"
              value={searchKeyword}
              onChange={(event) => setSearchKeyword(event.target.value)}
              placeholder="Tìm bệnh viện, phòng khám, bác sĩ..."
            />
            <button type="button" className="search-button">
              <Search size={20} />
            </button>
          </div>

          <div className="service-grid">
            {specialties.map((item) => {
              const Icon = item.icon;
              return (
                <article key={item.id} className="service-card">
                  <div className="service-icon">
                    <Icon size={28} />
                  </div>
                  <h3>{item.label}</h3>
                </article>
              );
            })}
          </div>
        </div>
      </section>

      <section id="doctors" className="section-block">
        <div className="container">
          <div className="section-head">
            <div>
              <span>Đặt khám bác sĩ</span>
              <h2>Danh sách bác sĩ nổi bật</h2>
              <p>Chọn bác sĩ theo chuyên khoa, cơ sở y tế và mức phí phù hợp.</p>
            </div>
            <button type="button" className="outline-button" onClick={() => setShowQuickBooking(true)}>
              Đặt khám nhanh
              <ArrowRight size={16} />
            </button>
          </div>

          <div className="doctor-list-grid">
            {filteredDoctors.map((doctor) => (
              <article key={doctor.id} className="doctor-web-card">
                <div className="doctor-avatar">{doctor.name.slice(0, 2)}</div>
                <h3>{doctor.name}</h3>
                <span className="doctor-badge">Bác sĩ</span>
                <ul>
                  <li>
                    <Stethoscope size={16} />
                    <span>{doctor.specialty}</span>
                  </li>
                  <li>
                    <Building2 size={16} />
                    <span>{doctor.clinic}</span>
                  </li>
                  <li>
                    <CalendarDays size={16} />
                    <span>{doctor.schedule}</span>
                  </li>
                </ul>
                <div className="doctor-card-footer">
                  <strong>{doctor.price}</strong>
                  <button type="button" className="link-button" onClick={() => setShowQuickBooking(true)}>
                    Xem thêm
                  </button>
                </div>
              </article>
            ))}
          </div>
        </div>
      </section>
    </div>
  );

  const renderPatientScreen = () => (
    <div className="role-page">
      <header className="role-header">
        <div className="container role-header-inner">
          <div>
            <span className="role-mini">Khu vực bệnh nhân</span>
            <h1>Xin chào, {currentUser.fullName}</h1>
          </div>
          <div className="role-header-actions">
            <button type="button" className={roleTab === 'overview' ? 'tab-button active' : 'tab-button'} onClick={() => setRoleTab('overview')}>
              Tổng quan
            </button>
            <button type="button" className={roleTab === 'booking' ? 'tab-button active' : 'tab-button'} onClick={() => setRoleTab('booking')}>
              Đặt lịch
            </button>
            <button type="button" className={roleTab === 'appointments' ? 'tab-button active' : 'tab-button'} onClick={() => setRoleTab('appointments')}>
              Lịch của tôi
            </button>
            <button type="button" className="text-button" onClick={handleLogout}>
              <LogOut size={16} />
              Đăng xuất
            </button>
          </div>
        </div>
      </header>

      <main className="container role-content">
        {roleTab === 'overview' && (
          <div className="role-grid">
            <section className="content-card">
              <h2>Thông tin nhanh</h2>
              <div className="info-stats">
                <div>
                  <span>Lịch đã đặt</span>
                  <strong>{patientAppointments.length}</strong>
                </div>
                <div>
                  <span>Tài khoản</span>
                  <strong>{roleLabel[currentUser.role]}</strong>
                </div>
                <div>
                  <span>Số điện thoại</span>
                  <strong>{currentUser.phone}</strong>
                </div>
              </div>
            </section>

            <section className="content-card">
              <h2>Lịch khám sắp tới</h2>
              <div className="list-stack">
                {patientAppointments.map((item) => (
                  <article key={item.id} className="schedule-item">
                    <div>
                      <h3>{item.doctorName}</h3>
                      <p>{item.specialty} • {item.clinic}</p>
                    </div>
                    <div className="schedule-meta">
                      <span>{formatDate(item.date)}</span>
                      <span>{item.time}</span>
                      <strong>{item.status}</strong>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          </div>
        )}

        {roleTab === 'booking' && (
          <div className="role-grid single">
            <section className="content-card">
              <h2>Đặt lịch khám</h2>
              <p className="section-note">Chọn bác sĩ, ngày giờ và nhập lý do khám.</p>
              {bookingMessage ? <div className="success-box">{bookingMessage}</div> : null}
              <form className="form-grid" onSubmit={handleBookingSubmit}>
                <div className="two-columns">
                  <label>
                    Bác sĩ
                    <select
                      value={bookingForm.doctorId}
                      onChange={(event) =>
                        setBookingForm((current) => ({ ...current, doctorId: event.target.value }))
                      }
                    >
                      {doctors.map((doctor) => (
                        <option key={doctor.id} value={doctor.id}>
                          {doctor.name} - {doctor.specialty}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label>
                    Ngày khám
                    <input
                      type="date"
                      value={bookingForm.date}
                      onChange={(event) =>
                        setBookingForm((current) => ({ ...current, date: event.target.value }))
                      }
                    />
                  </label>
                </div>
                <div className="two-columns">
                  <label>
                    Giờ khám
                    <input
                      type="time"
                      value={bookingForm.time}
                      onChange={(event) =>
                        setBookingForm((current) => ({ ...current, time: event.target.value }))
                      }
                    />
                  </label>
                  <label>
                    Chuyên khoa
                    <input type="text" value={selectedDoctor.specialty} readOnly />
                  </label>
                </div>
                <label>
                  Lý do khám
                  <textarea
                    rows={4}
                    value={bookingForm.reason}
                    onChange={(event) =>
                      setBookingForm((current) => ({ ...current, reason: event.target.value }))
                    }
                    placeholder="Mô tả triệu chứng hoặc nhu cầu khám của bạn"
                    required
                  />
                </label>
                <button type="submit" className="primary-button submit-button">
                  Gửi lịch hẹn
                </button>
              </form>
            </section>
          </div>
        )}

        {roleTab === 'appointments' && (
          <div className="role-grid single">
            <section className="content-card">
              <h2>Lịch hẹn của tôi</h2>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Mã lịch</th>
                    <th>Bác sĩ</th>
                    <th>Ngày khám</th>
                    <th>Giờ</th>
                    <th>Trạng thái</th>
                  </tr>
                </thead>
                <tbody>
                  {patientAppointments.map((item) => (
                    <tr key={item.id}>
                      <td>{item.id}</td>
                      <td>{item.doctorName}</td>
                      <td>{formatDate(item.date)}</td>
                      <td>{item.time}</td>
                      <td>{item.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>
          </div>
        )}
      </main>
    </div>
  );

  const renderDoctorScreen = () => (
    <div className="role-page">
      <header className="role-header doctor">
        <div className="container role-header-inner">
          <div>
            <span className="role-mini">Màn hình bác sĩ</span>
            <h1>{currentUser.fullName}</h1>
          </div>
          <div className="role-header-actions">
            <button type="button" className={roleTab === 'overview' ? 'tab-button active' : 'tab-button'} onClick={() => setRoleTab('overview')}>
              Lịch hôm nay
            </button>
            <button type="button" className={roleTab === 'appointments' ? 'tab-button active' : 'tab-button'} onClick={() => setRoleTab('appointments')}>
              Danh sách bệnh nhân
            </button>
            <button type="button" className="text-button" onClick={handleLogout}>
              <LogOut size={16} />
              Đăng xuất
            </button>
          </div>
        </div>
      </header>

      <main className="container role-content">
        <section className="content-card">
          <h2>Lịch khám theo bác sĩ</h2>
          <table className="data-table">
            <thead>
              <tr>
                <th>Mã lịch</th>
                <th>Bệnh nhân</th>
                <th>Lý do khám</th>
                <th>Ngày khám</th>
                <th>Giờ</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {doctorAppointments.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td>{item.patientName}</td>
                  <td>{item.reason}</td>
                  <td>{formatDate(item.date)}</td>
                  <td>{item.time}</td>
                  <td>{item.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
      </main>
    </div>
  );

  const renderAdminScreen = () => (
    <div className="role-page">
      <header className="role-header admin">
        <div className="container role-header-inner">
          <div>
            <span className="role-mini">Màn hình quản trị</span>
            <h1>Quản lý lịch hẹn và phân quyền</h1>
          </div>
          <div className="role-header-actions">
            <button type="button" className={roleTab === 'overview' ? 'tab-button active' : 'tab-button'} onClick={() => setRoleTab('overview')}>
              Tài khoản
            </button>
            <button type="button" className={roleTab === 'appointments' ? 'tab-button active' : 'tab-button'} onClick={() => setRoleTab('appointments')}>
              Toàn bộ lịch hẹn
            </button>
            <button type="button" className="text-button" onClick={handleLogout}>
              <LogOut size={16} />
              Đăng xuất
            </button>
          </div>
        </div>
      </header>

      <main className="container role-content">
        {roleTab === 'overview' && (
          <section className="content-card">
            <h2>Danh sách người dùng</h2>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Họ tên</th>
                  <th>Email</th>
                  <th>Số điện thoại</th>
                  <th>Vai trò</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id}>
                    <td>{user.fullName}</td>
                    <td>{user.email}</td>
                    <td>{user.phone}</td>
                    <td>{roleLabel[user.role]}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        )}

        {roleTab === 'appointments' && (
          <section className="content-card">
            <h2>Tất cả lịch hẹn</h2>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Mã lịch</th>
                  <th>Bệnh nhân</th>
                  <th>Bác sĩ</th>
                  <th>Chuyên khoa</th>
                  <th>Ngày khám</th>
                  <th>Trạng thái</th>
                </tr>
              </thead>
              <tbody>
                {appointments.map((item) => (
                  <tr key={item.id}>
                    <td>{item.id}</td>
                    <td>{item.patientName}</td>
                    <td>{item.doctorName}</td>
                    <td>{item.specialty}</td>
                    <td>{`${formatDate(item.date)} - ${item.time}`}</td>
                    <td>{item.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>
        )}
      </main>
    </div>
  );

  return (
    <>
      {!currentUser && renderPublicHome()}
      {currentUser?.role === 'PATIENT' && renderPatientScreen()}
      {currentUser?.role === 'DOCTOR' && renderDoctorScreen()}
      {currentUser?.role === 'ADMIN' && renderAdminScreen()}

      {showQuickBooking && (
        <div className="modal-overlay" onClick={() => setShowQuickBooking(false)}>
          <div className="booking-modal" onClick={(event) => event.stopPropagation()}>
            <button type="button" className="modal-close" onClick={() => setShowQuickBooking(false)}>
              <X size={24} />
            </button>
            <h2>Đặt khám nhanh</h2>
            <p>Vui lòng để lại thông tin, chúng tôi sẽ liên hệ lại với bạn để tư vấn.</p>
            {quickBookingMessage ? <div className="success-box">{quickBookingMessage}</div> : null}
            <form className="form-grid" onSubmit={handleQuickBookingSubmit}>
              <div className="two-columns">
                <label>
                  Họ tên *
                  <input
                    type="text"
                    value={quickBookingForm.fullName}
                    onChange={(event) =>
                      setQuickBookingForm((current) => ({ ...current, fullName: event.target.value }))
                    }
                    placeholder="VD: Nguyễn Văn A"
                    required
                  />
                </label>
                <label>
                  Số điện thoại *
                  <input
                    type="tel"
                    value={quickBookingForm.phone}
                    onChange={(event) =>
                      setQuickBookingForm((current) => ({ ...current, phone: event.target.value }))
                    }
                    placeholder="VD: 0912345678"
                    required
                  />
                </label>
              </div>
              <label>
                Cơ sở y tế *
                <input
                  type="text"
                  value={quickBookingForm.facility}
                  onChange={(event) =>
                    setQuickBookingForm((current) => ({ ...current, facility: event.target.value }))
                  }
                  placeholder="Nhập tên bệnh viện / phòng khám / bác sĩ"
                  required
                />
              </label>
              <label>
                Nhu cầu khám
                <textarea
                  rows={5}
                  value={quickBookingForm.need}
                  onChange={(event) =>
                    setQuickBookingForm((current) => ({ ...current, need: event.target.value }))
                  }
                  placeholder="Nhập nhu cầu khám chữa bệnh của bạn, mô tả triệu chứng hoặc dịch vụ muốn hẹn khám..."
                />
              </label>
              <button type="submit" className="primary-button submit-button">
                Gửi yêu cầu
              </button>
            </form>
          </div>
        </div>
      )}

      {showAuthModal && (
        <div className="modal-overlay" onClick={() => setShowAuthModal(false)}>
          <div className="auth-modal" onClick={(event) => event.stopPropagation()}>
            <button type="button" className="modal-close" onClick={() => setShowAuthModal(false)}>
              <X size={24} />
            </button>
            <div className="auth-tabs">
              <button type="button" className={authMode === 'login' ? 'active' : ''} onClick={() => setAuthMode('login')}>
                <LogIn size={16} />
                Đăng nhập
              </button>
              <button type="button" className={authMode === 'register' ? 'active' : ''} onClick={() => setAuthMode('register')}>
                <UserRoundPlus size={16} />
                Đăng ký
              </button>
            </div>

            {authMessage ? <div className="error-box">{authMessage}</div> : null}

            {authMode === 'login' ? (
              <form className="form-grid" onSubmit={handleLogin}>
                <h2>Đăng nhập hệ thống</h2>
                <label>
                  Email
                  <input
                    type="email"
                    value={loginForm.email}
                    onChange={(event) =>
                      setLoginForm((current) => ({ ...current, email: event.target.value }))
                    }
                    required
                  />
                </label>
                <label>
                  Mật khẩu
                  <input
                    type="password"
                    value={loginForm.password}
                    onChange={(event) =>
                      setLoginForm((current) => ({ ...current, password: event.target.value }))
                    }
                    required
                  />
                </label>
                <div className="sample-account-box">
                  <strong>Tài khoản mẫu:</strong>
                  <span>Bệnh nhân: `patient@clinic.vn` / `123456`</span>
                  <span>Bác sĩ: `doctor@clinic.vn` / `123456`</span>
                  <span>Quản trị: `admin@clinic.vn` / `123456`</span>
                </div>
                <button type="submit" className="primary-button submit-button">
                  Đăng nhập
                </button>
              </form>
            ) : (
              <form className="form-grid" onSubmit={handleRegister}>
                <h2>Tạo tài khoản mới</h2>
                <label>
                  Họ tên
                  <input
                    type="text"
                    value={registerForm.fullName}
                    onChange={(event) =>
                      setRegisterForm((current) => ({ ...current, fullName: event.target.value }))
                    }
                    required
                  />
                </label>
                <div className="two-columns">
                  <label>
                    Email
                    <input
                      type="email"
                      value={registerForm.email}
                      onChange={(event) =>
                        setRegisterForm((current) => ({ ...current, email: event.target.value }))
                      }
                      required
                    />
                  </label>
                  <label>
                    Số điện thoại
                    <input
                      type="tel"
                      value={registerForm.phone}
                      onChange={(event) =>
                        setRegisterForm((current) => ({ ...current, phone: event.target.value }))
                      }
                      required
                    />
                  </label>
                </div>
                <div className="two-columns">
                  <label>
                    Mật khẩu
                    <input
                      type="password"
                      value={registerForm.password}
                      onChange={(event) =>
                        setRegisterForm((current) => ({ ...current, password: event.target.value }))
                      }
                      required
                    />
                  </label>
                  <label>
                    Vai trò
                    <select
                      value={registerForm.role}
                      onChange={(event) =>
                        setRegisterForm((current) => ({ ...current, role: event.target.value }))
                      }
                    >
                      <option value="PATIENT">Bệnh nhân</option>
                      <option value="DOCTOR">Bác sĩ</option>
                      <option value="ADMIN">Quản trị viên</option>
                    </select>
                  </label>
                </div>
                <button type="submit" className="primary-button submit-button">
                  Đăng ký
                </button>
              </form>
            )}
          </div>
        </div>
      )}
    </>
  );
}

export default App;
