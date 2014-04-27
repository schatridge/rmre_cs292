function findGradeMean(col1,col2) {

  var grades = col1.find().map(function(u){return u.known_words_stemmed});
  var user = col1.distinct("user_name");
  var global = col2.find().map(function(u){return u.words})[0];

  for (var i=0; i<grades.length; i++) {

    var k = 0;
    var mean = 0;
    var total = 0;
    var matching = true;
 
    for (var j=0; j<grades[i].length; j++) {

      total = total + (grades[i])[j].str;
      var hold = k;

      while (matching) {

        if ((grades[i])[j].word == global[k].word) {

          matching = false;
	  var level = (global[k].difficulty_level)*((grades[i])[j].str);
          mean = mean + level;

        }

        k = k+1;

        if (k == global.length) {

          k = hold;
          matching = false;

        }

      }

      matching = true;

    }

    var average = mean/total; 
    col1.update({user_name:user[i]},{$set:{mean_word_level:average}});

  }

}
