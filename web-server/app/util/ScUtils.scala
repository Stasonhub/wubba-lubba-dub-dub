package util

object ScUtils {

  /**
    * Searches first element of desc ordered list which is greater or equal to value
    */
  def binarySearchFirstGreaterOrEq(itemsDesc: Seq[Long], value: Long): Option[Int] = {
    def bS(itemsDesc: Seq[Long], value: Long)(startIndex: Int = 0, endIndex: Int = itemsDesc.length - 1): Option[Int] = {
      if (itemsDesc(startIndex) <= value) return Some(startIndex)
      if (itemsDesc(endIndex) > value) return None

      if (endIndex - startIndex < 2) {
        if (itemsDesc(endIndex) <= value) return Some(endIndex)
        if (itemsDesc(startIndex) > value) return None
      }

      val mid = startIndex + (endIndex - startIndex) / 2
      if (itemsDesc(mid) <= value) {
        bS(itemsDesc, value)(startIndex, mid)
      } else {
        bS(itemsDesc, value)(mid + 1, endIndex)
      }
    }

    bS(itemsDesc, value)()
  }


}
