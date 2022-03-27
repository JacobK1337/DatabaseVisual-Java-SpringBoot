document.querySelectorAll('.testSubmit').forEach(button => {
    button.addEventListener('click', () => {
        const databaseForm = button.nextElementSibling;
        button.classList.toggle('create-database--form--active');
        if (button.classList.contains('create-database--form--active')) {
            databaseForm.style.maxHeight = databaseForm.scrollHeight + 'px';
        } else {
            databaseForm.style.maxHeight = 0;
        }
    });
});