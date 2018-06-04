
def time[R](msg : String = "Elapsed")(block: => R): R = {
    val t0 = System.currentTimeMillis()
    val result = block
    val t1 = System.currentTimeMillis()
    if (t1-t0 > 10000)println(msg  + " time: "+ (t1 - t0) / 1000 + "s")
    else println(msg + " time: " + (t1-t0)+"ms")
    result
}


def ClusterFile(filePath : String, both : Boolean = false) = {
    println(s"Testing Address Clustering with data from $filePath")
    val set_of_sets = time("reading data"){
        import scala.io._
        val bufferedSource = Source.fromFile(filePath)
        val lines = bufferedSource.getLines
        lines.next() // if saved with windows remove BOM from first line
        // read lines, split at comma, trim, parse to int, convert each line to set and all lines to set of sets
        val set_of_sets = lines.map(_.split(",").map(_.trim).map(_.toInt).toSet).toSet
        bufferedSource.close
        set_of_sets
    }
    // clustering
    import linking.common._
    val m2 = time("mutable"){
        val result_iterator = Clustering.getClustersMutable(set_of_sets)
        // Output processing
        val representatives = result_iterator.toList
        // representatives.foreach(println)
        val clusters = representatives
        .groupBy(_.cluster) // group by cluster identifier
        .mapValues(_.map(_.id).sorted) // map from  Map[cluster -> List[Result(id,cluster)]] to Map[cluster -> List[id]] and sort
        // println(clusters.values.map(_.size))
        clusters.values
    }
    if(both){ // this can get really slow fast
        val m1 = time("immutable"){
            val result_iterator = Clustering.getClusters(set_of_sets)
            // Output processing
            val representatives = result_iterator.toList
            // representatives.foreach(println)
            val clusters = representatives
            .groupBy(_.cluster) // group by cluster identifier
            .mapValues(_.map(_.id).sorted) // map from  Map[cluster -> List[Result(id,cluster)]] to Map[cluster -> List[id]] and sort
            // println(clusters.values.map(_.size))
            clusters.values
        }
        // println(m1)
        // println(m2)
        println("Difference between clusters: " + (m1.toSet diff m2.toSet).size)
    }
}

def RandomDataCluster(num_sets: Int = 100000, num_addrs: Int = 100000, max_addrs: Int = 15, both : Boolean = false) {
    println("Testing Address Clustering with random data:")
    // Some Testdata
    val input_sets = Set(Set(1, 2, 3),Set(4, 5, 6),Set(7, 8, 9),Set(3, 4))
    // More test data, randomly generated
    val r = scala.util.Random
    val more_sets = (1 to num_sets) // 
        .map(_ => 1 to (2 + r.nextInt(max_addrs -2))) // number of elements 
        .map(x => x.map(_ => r.nextInt(num_addrs)).toSet).toSet // map to addrs

    // Clustering:
    import linking.common._
    val m2 = time("mutable"){
        val result_iterator = Clustering.getClustersMutable(more_sets)
        // Output processing
        val representatives = result_iterator.toList
        // representatives.foreach(println)
        val clusters = representatives
        .groupBy(_.cluster) // group by cluster identifier
        .mapValues(_.map(_.id).sorted) // map from  Map[cluster -> List[Result(id,cluster)]] to Map[cluster -> List[id]] and sort
        println(clusters.values.map(_.size))
        clusters.values
    }
    if(both){ // this can get really slow fast
        val m1 = time("immutable"){
            val result_iterator = Clustering.getClusters(more_sets)
            // Output processing
            val representatives = result_iterator.toList
            // representatives.foreach(println)
            val clusters = representatives
            .groupBy(_.cluster) // group by cluster identifier
            .mapValues(_.map(_.id).sorted) // map from  Map[cluster -> List[Result(id,cluster)]] to Map[cluster -> List[id]] and sort
            println(clusters.values.map(_.size))
            clusters.values
        }
        // println(m1)
        // println(m2)
        println("Difference between clusters: " + (m1.toSet diff m2.toSet).size)
    }
  }

RandomDataCluster(100,100,3, true)
val filePath = "./sample_data/btc100K.csv"
ClusterFile(filePath, true)