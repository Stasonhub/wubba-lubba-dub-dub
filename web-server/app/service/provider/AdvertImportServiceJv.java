package service.provider;

//@Singleton
public class AdvertImportServiceJv {
//
//    private Logger logger = LoggerFactory.getLogger(AdvertImportServiceJv.class);
//
//    private List<AdvertsProvider> advertsProviders;
//
//    private LocationServiceJv locationServiceJv;
//    private AdvertImportRepositoryJv advertImportMapper;
//    private AdvertRepositoryJv advertMapper;
//    private PhotoRepositoryJv photoMapper;
//    private UserRepositoryJv userMapper;
//    private PhotoService photoService;
//    private PhotoPersistService photoContentService;
//
//    @Inject
//    public AdvertImportServiceJv(AvitoAdvertsProvider avitoAdvertsProvider,
//                                 TotookAdvertsProvider totookAdvertsProvider,
//                                 LocationServiceJv locationServiceJv,
//                                 AdvertImportRepositoryJv advertImportMapper,
//                                 AdvertRepositoryJv advertMapper,
//                                 PhotoRepositoryJv photoMapper,
//                                 UserRepositoryJv userMapper,
//                                 PhotoService photoService,
//                                 PhotoPersistService photoContentService) {
//        this.advertsProviders = Arrays.asList(avitoAdvertsProvider, totookAdvertsProvider);
//        this.locationServiceJv = locationServiceJv;
//        this.advertImportMapper = advertImportMapper;
//        this.advertMapper = advertMapper;
//        this.photoMapper = photoMapper;
//        this.userMapper = userMapper;
//        this.photoService = photoService;
//        this.photoContentService = photoContentService;
//    }
//
//    public List<String> getProviderTypes() {
//        return advertsProviders.stream().map(AdvertsProvider::getType).collect(Collectors.toList());
//    }
//
//    public long getLastImportTime(String typeName) {
//        return advertImportMapper.getLastImportTime(typeName);
//    }
//
//
//    public void runImport(String type) {
//        Objects.requireNonNull(type);
//        Optional<AdvertsProvider> advertsProvider =
//                advertsProviders.stream().filter(advertProvider -> advertProvider.getType().equals(type)).findFirst();
//        if (!advertsProvider.isPresent()) {
//            throw new IllegalArgumentException("Unknown type: " + type);
//        }
//
//        runImport(advertsProvider.get());
//    }
//
//    public void runImport() {
//        // run for importers
//        advertsProviders.stream().filter(v -> !v.isVerifier()).forEach(this::runImport);
//        // run for verifiers
//        advertsProviders.stream().filter(AdvertsProvider::isVerifier).forEach(this::runImport);
//    }
//
//    private void runImport(AdvertsProvider advertsProvider) {
//        logger.info("Started import {}", advertsProvider.getType());
//
//        long lastImportTime = advertImportMapper.getLastImportTime(advertsProvider.getType());
//
//        Long firstAdvertTs = null;
//        Iterator<ParsedAdvertHeader> adverts = advertsProvider.getHeaders();
//        logger.info("Got headers for type {}", advertsProvider.getType());
//        int maxItemsToScan = advertsProvider.getMaxItemsToScan();
//        int i = 0;
//        int verified = 0;
//        while (i < maxItemsToScan && adverts.hasNext()) {
//            ParsedAdvertHeader advertHeader = adverts.next();
//            if (advertHeader.getPublicationTimestamp() <= lastImportTime) {
//                logger.info("Stopping scan. Last import ts={}, advert publication is {}. Advert {}",
//                        lastImportTime, advertHeader.getPublicationTimestamp(), advertHeader.getAdvertUrl());
//                break;
//            }
//            if (firstAdvertTs == null) {
//                firstAdvertTs = advertHeader.getPublicationTimestamp();
//            }
//            try {
//                ParsedAdvert advert = advertsProvider.getAdvert(advertHeader);
//                if (advertsProvider.isVerifier()) {
//                    logger.info("Verifying advert {} for type {}", advert, advertsProvider.getType());
//                    if (verifyAdvert(advert)) {
//                        verified++;
//                    }
//                } else {
//                    logger.info("Checking/persisting advert {} for type {}", advert, advertsProvider.getType());
//                    if (checkAdvert(advert)) {
//                        persistAdvert(advertsProvider, advert);
//                    } else {
//                        logger.info("Advert {} is not correct, ignored", advert, advertsProvider.getType());
//                    }
//                }
//            } catch (Exception e) {
//                logger.warn("Failed to process advert {}", advertHeader, e);
//            }
//
//            i++;
//        }
//
//        if (advertsProvider.isVerifier()) {
//            logger.info("Advert provider {} verification rate is {} on {} adverts", advertsProvider.getType(), verified / (float) i, i);
//        }
//
//        // save first advert import time (latest by value)
//        if (firstAdvertTs != null) {
//            logger.info("Saving import time {} for {}", firstAdvertTs, advertsProvider.getType());
//            advertImportMapper.saveLastImportTime(advertsProvider.getType(), firstAdvertTs);
//        }
//    }
//
//    private boolean checkAdvert(ParsedAdvert parsedAdvert) {
//        if (checkAndWarn(() -> StringUtils.isEmpty(parsedAdvert.getAddress()),
//                () -> logger.warn("Address is empty for advert {}", parsedAdvert))) {
//            return false;
//        } else if (checkAndWarn(() -> parsedAdvert.getRooms() == null,
//                () -> logger.warn("Rooms is null for advert {}", parsedAdvert))) {
//            return false;
//        } else if (checkAndWarn(() -> parsedAdvert.getPrice() == null,
//                () -> logger.warn("Price is empty for advert {}", parsedAdvert))) {
//            return false;
//        } else if (checkAndWarn(() -> parsedAdvert.getPhotos() == null || parsedAdvert.getPhotos().isEmpty(),
//                () -> logger.warn("Photos is empty for advert {}", parsedAdvert))) {
//            return false;
//        }
//        return true;
//    }
//
//    private boolean verifyAdvert(ParsedAdvert parsedAdvert) throws IOException {
//        List<Advert> matchingAdverts = advertMapper.findBySqPriceCoords(parsedAdvert.getSq(), parsedAdvert.getPrice(), parsedAdvert.getLatitude(), parsedAdvert.getLongitude());
//        if (matchingAdverts.isEmpty()) {
//            logger.warn("Verification. Failed to find advert by [sq/price/lat/lon]: [{},{},{},{}]", parsedAdvert.getSq(), parsedAdvert.getPrice(), parsedAdvert.getLatitude(), parsedAdvert.getLongitude());
//            return false;
//        }
//
//        if (matchingAdverts.size() > 1) {
//            logger.warn("Verification. Found more than one matching adverts for {}", parsedAdvert);
//            return false;
//        }
//
//        Advert matchingAdvert = matchingAdverts.get(0);
//        // find match by advert/partial user phone
//        List<User> matchingUsers = userMapper.findByStartingSixNumbers(matchingAdvert.id(), (int) parsedAdvert.getPhone());
//        if (matchingUsers.isEmpty()) {
//            logger.warn("Verification. Failed to find user for advert {} with number {}", parsedAdvert, parsedAdvert.getPhone());
//            return false;
//        }
//
//        if (matchingUsers.size() > 1) {
//            logger.warn("Verification. Found more than one matching users for advert {}. Number {}. Users {}", parsedAdvert, parsedAdvert.getPhone(),
//                    matchingUsers.stream().map(User::id).collect(Collectors.toList()));
//            return false;
//        }
//
//        // set current user new rate
//        // set other users /4 rate
//        userMapper.arrangeRate(matchingAdvert.id(), matchingUsers.get(0).id(), parsedAdvert.getTrustRate(), 0.25);
//        return true;
//    }
//
//    private boolean persistAdvert(AdvertsProvider advertsProvider, ParsedAdvert parsedAdvert) throws IOException {
//        List<Photo> photos = photoContentService.savePhotos(advertsProvider.getType(), parsedAdvert);
//
//        Advert matchingAdvert = findMatchingAdvertByPhotos(parsedAdvert, photos);
//        User matchingUser = userMapper.findByPhone(parsedAdvert.getPhone());
//
//        if (matchingAdvert != null) {
//            /* full duplicate */
//            if (matchingUser != null) {
//                return false;
//            }
//
//            // found new user for the same advert
//            // remove half of trust
//            User user = new User(
//                    0,
//                    parsedAdvert.getPhone(),
//                    parsedAdvert.getUserName(),
//                    parsedAdvert.getTrustRate() / 2,
//                    Option.empty(),
//                    false
//
//            );
//            userMapper.createUser(user);
//            advertMapper.bindToUser(matchingAdvert.id(), user.id());
//            return false;
//        }
//
//        Advert advert = advertMapper.createAdvert(new Advert(0,
//                parsedAdvert.getPublicationTimestamp(),
//                locationServiceJv.getDistrictFromAddress(parsedAdvert.getLatitude(), parsedAdvert.getLongitude()),
//                parsedAdvert.getAddress(),
//                parsedAdvert.getFloor(),
//                parsedAdvert.getMaxFloor(),
//                parsedAdvert.getRooms(),
//                parsedAdvert.getSq(),
//                parsedAdvert.getPrice(),
//                true,
//                0,
//                parsedAdvert.getDescription(),
//                parsedAdvert.getLatitude(),
//                parsedAdvert.getLongitude(),
//                parsedAdvert.getBedrooms(),
//                parsedAdvert.getBeds(),
//                advertsProvider.getType(),
//                parsedAdvert.getOriginId()
//                ));
//
//        if (matchingUser != null) {
//            // found another one advert from the same user
//            // remove 4x trust
//            User user = new User(
//                    matchingUser.id(),
//                    matchingUser.phone(),
//                    matchingUser.name(),
//                    matchingUser.trustRate() / 4,
//                    matchingUser.password(),
//                    matchingUser.registered()
//
//            );
//            userMapper.updateUser(user);
//            advertMapper.bindToUser(advert.id(), matchingUser.id());
//        } else {
//            // just create new user and bind advert
//            User user = userMapper.createUser(new User(
//                    0,
//                    parsedAdvert.getPhone(),
//                    parsedAdvert.getUserName(),
//                    parsedAdvert.getTrustRate(),
//                    Option.empty(),
//                    false
//            ));
//            advertMapper.bindToUser(advert.id(), user.id());
//        }
//
//        // persist photos
//        for (Photo photo : photos) {
//            photoMapper.createPhoto(new Photo(
//                    0,
//                    advert.id(),
//                    photo.path(),
//                    photo.main(),
//                    photo.hash()
//            ));
//        }
//
//        return true;
//    }
//
//    private Advert findMatchingAdvertByPhotos(ParsedAdvert parsedAdvert, List<Photo> photos) {
//        Iterable<Photo> photoIterable = JavaConverters.collectionAsScalaIterableConverter(photos).asScala();
//        scala.collection.immutable.List<Object> matchingAdverts = photoService.matchingAdverts(photoIterable.toList());
//
//        if (matchingAdverts.size() > 1) {
//            logger.error("For incoming advert {} found more than 1 duplicates {}", parsedAdvert, matchingAdverts);
//            return advertMapper.findById(matchingAdverts.get(0));
//        }
//
//        if (matchingAdverts.size() > 0) {
//            logger.warn("For incoming advert {} found duplicate {}", parsedAdvert, matchingAdverts.get(0));
//            return advertMapper.findById(matchingAdverts.get(0));
//        }
//        return null;
//    }
//
//    private boolean checkAndWarn(Supplier<Boolean> checker, Runnable warner) {
//        boolean value = checker.get();
//        if (value) {
//            warner.run();
//        }
//        return value;
//    }

}