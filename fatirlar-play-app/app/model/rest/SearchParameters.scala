package model.rest

import model.District

case class SearchParameters(districts: List[District], priceRange: (Int, Int), rooms1: Boolean, rooms2: Boolean, rooms3: Boolean, page: Int)
