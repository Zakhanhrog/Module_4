// src/main/resources/static/js/memory-form.js
document.addEventListener('DOMContentLoaded', function () {
    const newFilesInput = document.getElementById('newFiles');
    const newFilesPreviewContainer = document.getElementById('newFilesPreview');

    if (newFilesInput && newFilesPreviewContainer) {
        newFilesInput.addEventListener('change', function (event) {
            newFilesPreviewContainer.innerHTML = ''; // Xóa preview cũ

            const files = event.target.files;
            if (files.length > 0) {
                const previewTitle = document.createElement('h6');
                previewTitle.className = 'col-12 fw-medium mb-2 mt-2';
                previewTitle.textContent = newFilesPreviewContainer.dataset.previewTitle || 'Selected files to upload:'; // Lấy từ data-attribute nếu có
                newFilesPreviewContainer.appendChild(previewTitle);
            }


            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                const reader = new FileReader();

                const colDiv = document.createElement('div');
                colDiv.className = 'col-md-3 col-sm-4 col-6 mb-2'; // Responsive columns

                const previewItemDiv = document.createElement('div');
                previewItemDiv.className = 'new-file-preview-item text-center h-100';

                if (file.type.startsWith('image/')) {
                    reader.onload = function (e) {
                        const img = document.createElement('img');
                        img.src = e.target.result;
                        img.alt = file.name;
                        img.className = 'new-file-preview-img img-thumbnail';
                        previewItemDiv.appendChild(img);
                    }
                    reader.readAsDataURL(file);
                } else {
                    // Placeholder cho các loại file khác
                    const icon = document.createElement('i');
                    let fileIconClass = 'fas fa-file-alt fa-2x text-muted'; // Icon mặc định
                    if (file.type.startsWith('video/')) {
                        fileIconClass = 'fas fa-file-video fa-2x text-muted';
                    } else if (file.type.startsWith('audio/')) {
                        fileIconClass = 'fas fa-file-audio fa-2x text-muted';
                    }
                    icon.className = fileIconClass;

                    const placeholderDiv = document.createElement('div');
                    placeholderDiv.className = 'd-flex flex-column align-items-center justify-content-center new-file-preview-img'; // Giữ kích thước tương tự ảnh
                    placeholderDiv.style.height = '100px'; // Chiều cao tương đương ảnh
                    placeholderDiv.style.border = '1px dashed #ccc';
                    placeholderDiv.appendChild(icon);
                    previewItemDiv.appendChild(placeholderDiv);
                }

                const fileNameDiv = document.createElement('div');
                fileNameDiv.className = 'new-file-name mt-1';
                fileNameDiv.textContent = file.name;
                previewItemDiv.appendChild(fileNameDiv);

                colDiv.appendChild(previewItemDiv);
                newFilesPreviewContainer.appendChild(colDiv);
            }
        });
    }

    // Khởi tạo lại tooltips nếu có trên trang này (cho nút xóa file hiện tại)
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    })
});