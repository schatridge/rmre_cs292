function findTextMean(col2) {

  var stems = col2.find().map(function(u){return u.words});
  
  for (var i=0; i<stems.length; i++) {

    var mean = 0;
    var total = 0;
 
    for (var j=0; j<stems[i].length; j++) {

      var level = (stems[i])[j].difficulty_level*(stems[i])[j].frequency;
      mean = mean + level;

      total = total + (stems[i])[j].frequency;

    }

    var name = col2.distinct("title");
    var average = mean/total;
  
    col2.update({title:name[i]},{$set:{mean_word_level:average}});

  }

}
