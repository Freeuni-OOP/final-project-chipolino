import {AdminPanel} from "./pages/admin/AdminPanel.jsx";
// Если у тебя есть файл с глобальными стилями, раскомментируй строку ниже:
// import './App.css'

export default function App() {
    return (
        <div style={{
            maxWidth: '1200px',
            margin: '0 auto',
            padding: '20px'
        }}>
            <AdminPanel />
        </div>
    )
}