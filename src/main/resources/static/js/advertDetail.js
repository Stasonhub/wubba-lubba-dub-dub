;(function () {

    'use strict';

    $(document).ready(function () {
        $('#gal2').lightGallery({
            selector: '.gallery-item'
        });

        var showUser = $('#show-user');
        showUser.on('click', function () {
            var advertId = showUser.getAttribute('');
            
        });
    });

    ymaps.ready(function () {
        var map = new ymaps.Map("adv-detail-map", {
            center: [55.76, 37.64],
            zoom: 13
        });
    });
}());