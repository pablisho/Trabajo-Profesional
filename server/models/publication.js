var mongoose = require('mongoose'),  
    Schema   = mongoose.Schema;

var PublicationSchema = new Schema({  
  title:    { type: String, required:true},
  summary:  { type: String, required:true},
  price:    { type: Number, required:true},
  image:   { type: String},
  ar_obj:   { type: []},
  date:     { type: Date },
  user_id:  { type: Schema.Types.ObjectId, required:true},
  cant:     { type: Number, required:true},
  sells:    { type: Number, default: 0}  
});

module.exports = mongoose.model('Publication', PublicationSchema); 
