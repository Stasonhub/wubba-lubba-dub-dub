package util

object ScUtils {

  /**
    * Searches of index last element of desc ordered list which is smallest number greater or equal to value
    */
  def binarySearchSmallestGreaterThanOrEq(itemsDesc: Seq[Long], value: Long): Option[Int] = {
    def bS(itemsDesc: Seq[Long], value: Long)(startIndex: Int = 0, endIndex: Int = itemsDesc.length - 1): Option[Int] = {
      if (itemsDesc(startIndex) < value) return None
      if (itemsDesc(endIndex) >= value) return Some(endIndex)

      if (endIndex - startIndex < 2) {
        if (itemsDesc(startIndex) >= value) return Some(startIndex)
        if (itemsDesc(endIndex) < value) return None
      }

      val mid = startIndex + (endIndex - startIndex) / 2
      if (itemsDesc(mid) >= value) {
        bS(itemsDesc, value)(mid, endIndex)
      } else {
        bS(itemsDesc, value)(startIndex, mid - 1)
      }
    }

    bS(itemsDesc, value)()
  }


}
