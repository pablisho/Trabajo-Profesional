var mongoose = require('mongoose'),
    Schema   = mongoose.Schema;

var TransactionSchema = new Schema({
  pub_id:  { type: Schema.Types.ObjectId, required:true},
  buyer_id: { type: Schema.Types.ObjectId, required:true},
  seller_id: { type: Schema.Types.ObjectId, required:true},
  date: { type: Date, default: Date.now},
  price: { type: Number, required:true}
});

module.exports = mongoose.model('Transaction', TransactionSchema);

