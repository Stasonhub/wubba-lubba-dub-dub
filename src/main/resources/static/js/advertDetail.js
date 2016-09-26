;(function () {

    'use strict';

    $(document).ready(function () {
        $("#advert-carousel").owlCarousel({
            autoPlay: 3000,
            stopOnHover: true,
            navigation: true,
            paginationSpeed: 1000,
            goToFirstSpeed: 2000,
            singleItem: true,
            autoHeight: true,
            transitionStyle: "fade"
        });
    });
}());