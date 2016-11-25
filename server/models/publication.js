var mongoose = require('mongoose'),  
    Schema   = mongoose.Schema;

var PublicationSchema = new Schema({  
  title:    { type: String, required:true},
  summary:  { type: String, required:true},
  price:    { type: Number, required:true},
  images:   { type: []},
  ar_obj:   { type: String},
  date:     { type: Date },
  user_id:  { type: Schema.Types.ObjectId, required:true}
});

module.exports = mongoose.model('Publication', PublicationSchema); 
