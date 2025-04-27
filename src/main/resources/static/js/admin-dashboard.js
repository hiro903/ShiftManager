/**
 * 管理者ダッシュボード用JavaScript
 */
document.addEventListener('DOMContentLoaded', function() {
    // モバイル用のサイドバー切り替え
    const toggleSidebarBtn = document.querySelector('.sidebar-toggle');
    if (toggleSidebarBtn) {
        toggleSidebarBtn.addEventListener('click', function() {
            document.getElementById('sidebar').classList.toggle('show');
        });
    }

    // 通知サンプル
    const notifications = [
        { title: '新規シフト提出', message: '田中一郎さんが12月分のシフトを提出しました', time: '5分前' },
        { title: 'シフト申請', message: '佐藤花子さんが11/22のシフト変更を希望しています', time: '1時間前' },
        { title: 'システム通知', message: '12月分のシフト確定期限は11/25です', time: '3時間前' }
    ];

    // 通知ドロップダウンを生成
    const notificationDropdown = document.querySelector('.notification-dropdown');
    if (notificationDropdown) {
        const notificationList = notificationDropdown.querySelector('.notification-list');
        
        notifications.forEach(notification => {
            const item = document.createElement('a');
            item.classList.add('dropdown-item', 'notification-item');
            item.href = '#';
            
            item.innerHTML = `
                <div class="d-flex">
                    <div class="flex-grow-1">
                        <h6 class="mb-1">${notification.title}</h6>
                        <p class="mb-0 text-muted">${notification.message}</p>
                        <small class="text-muted">${notification.time}</small>
                    </div>
                </div>
            `;
            
            notificationList.appendChild(item);
        });
        
        // 通知数バッジを更新
        const notificationBadge = document.querySelector('.notification-badge');
        if (notificationBadge) {
            notificationBadge.textContent = notifications.length;
        }
    }

    // 従業員シフト提出状況グラフ
    const shiftSubmissionCtx = document.getElementById('shiftSubmissionChart');
    if (shiftSubmissionCtx) {
        new Chart(shiftSubmissionCtx, {
            type: 'doughnut',
            data: {
                labels: ['提出済み', '未提出'],
                datasets: [{
                    data: [13, 2],
                    backgroundColor: ['#28a745', '#dc3545'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    }

    // 月次シフト状況グラフ
    const monthlyStatusCtx = document.getElementById('monthlyStatusChart');
    if (monthlyStatusCtx) {
        new Chart(monthlyStatusCtx, {
            type: 'bar',
            data: {
                labels: ['11月', '12月', '1月'],
                datasets: [
                    {
                        label: '確定済み',
                        data: [100, 30, 0],
                        backgroundColor: '#28a745'
                    },
                    {
                        label: '確認中',
                        data: [0, 70, 0],
                        backgroundColor: '#ffc107'
                    },
                    {
                        label: '未提出',
                        data: [0, 0, 100],
                        backgroundColor: '#dc3545'
                    }
                ]
            },
            options: {
                responsive: true,
                scales: {
                    x: {
                        stacked: true
                    },
                    y: {
                        stacked: true,
                        max: 100
                    }
                }
            }
        });
    }

    // エクスポートボタンの処理
    const exportBtn = document.querySelector('.export-btn');
    if (exportBtn) {
        exportBtn.addEventListener('click', function() {
            alert('シフトデータをエクスポートします。この機能は開発中です。');
        });
    }

    // 期間選択の変更処理
    const periodSelect = document.querySelector('.period-select');
    if (periodSelect) {
        periodSelect.addEventListener('change', function() {
            console.log('選択された期間: ' + this.value);
            // ここで期間に応じたデータ更新処理を実装
        });
    }
}); 