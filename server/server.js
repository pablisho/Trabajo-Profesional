var express = require("express");  
var app = express();
var bodyParser  = require("body-parser");
var methodOverride = require("method-override");
var mongoose = require('mongoose');
var morgan = require('morgan');
var passport	= require('passport');
var jwt    = require('jwt-simple'); // used to create, sign, and verify tokens

var config = require('./config'); // get our config file
var User   = require('./models/user'); // get our mongoose model
var Publication = require('./models/publication');
var Transaction = require('./models/transaction');

mongoose.connect('mongodb://localhost/arbuy', function(err, res) {
  if(err) {
    console.log('ERROR: connecting to Database. ' + err);
  }
});

app.use(bodyParser.urlencoded({ extended: false }));  
app.use(bodyParser.json());  
app.use(methodOverride());
app.use(morgan('dev'));
app.use(passport.initialize());

// pass passport for configuration
require('./passport')(passport)

// BASIC API
app.get('/', function(req,res){
  res.send('Hello!!');
});


// API AUTENTICATION

// get an instance of the router for api routes
var apiRoutes = express.Router(); 

apiRoutes.post('/signup', function(req, res) {
  if (!req.body.username || !req.body.password) {
    res.json({success: false, msg: 'Please pass name and password.'});
  } else {
    var newUser = new User({
      username: req.body.username,
      password: req.body.password,
      email: req.body.email, 
      first_name: req.body.first_name,
      last_name: req.body.last_name,
      address: req.body.address,
      city: req.body.city,
      registerd: Date.now()
    });
    // save the user
    newUser.save(function(err) {
      if (err) {
        console.log(err);
        return res.json({success: false, msg: 'Username already exists.'});
        console.log(err);
      }
      res.json({success: true, msg: 'Successful created new user.'});
    });
  }
});



// route to authenticate a user (POST http://localhost:8080/api/authenticate)
apiRoutes.post('/authenticate', function(req, res) {

  // find the user
  User.findOne({
    username: req.body.username
  }, function(err, user) {

    if (err) throw err;

    if (!user) {
      res.json({ success: false, msg: 'Authentication failed. User not found.' });
    } else if (user) {
      // Check password.
        user.comparePassword(req.body.password, function (err, isMatch) {
        if (isMatch && !err) {
          // if user is found and password is right create a token
          var token = jwt.encode(user, config.secret);
          // return the information including token as JSON
          res.json({success: true, token: 'JWT ' + token});
        } else {
          res.send({success: false, msg: 'Authentication failed. Wrong password.'});
        }
      });

    }   
  });
});

apiRoutes.use(passport.authenticate('jwt', { session: false}));

apiRoutes.post('/publish',function(req,res){
  if (!req.body.title || !req.body.summary || !req.body.price || !req.body.cant) {
    res.json({success: false, msg: 'Please provide title, summary and price and cant.'});
  } else {
    var newPublication = new Publication({
      title:    req.body.title,
      summary:  req.body.summary,
      price:    req.body.price,
      date:     Date.now(),
      user_id:  req.user._id,
      cant: req.body.cant
    });
    // save the user
    newPublication.save(function(err) {
      if (err) {
        console.log(err);
        return res.json({success: false, msg: 'Error saving publication'});
      }
      res.json({success: true, msg: 'Successful created new publication.'});
    });
  }
});

apiRoutes.get('/publications', function(req,res){
  var pag = 0;
  var pagSize = 2;
  if(req.query.pag){
    pag = req.query.pag;
  }
  Publication.find({},{},{skip: pag*pagSize, limit: pagSize}, function(err, publications){
     if(err) res.send(500, err.message);
     res.status(200).json(publications);
  });
});

apiRoutes.get('/publication/:id', function(req,res){
  Publication.findById(req.params.id, function(err,publication){
    if(err) return res.send(500, err.message);
    res.status(200).json(publication);
  });
});

apiRoutes.get('/publications/:user_id', function(req,res){
  Publication.find({user_id : req.params.user_id}, function(err, publications){
    if(err) return res.send(500, err.message);
    res.status(200).json(publications);
  });
});

apiRoutes.post('/buy/:pub_id', function(req,res){
  Publication.findById(req.params.pub_id, function(err, publication){
    if(err) return res.send(500, err.message);
    if(publication.cant < 1) return res.json({success:false,msg: 'No hay cantidad suficiente'});
    publication.cant = publication.cant - 1;
    publication.sells = publication.sells + 1;
    publication.save();
    var newTransaction = new Transaction({
      pub_id: req.params.pub_id,
      buyer_id: req.user._id,
      seller_id: publication.user_id,
      price: publication.price
    });
    // save the user
    newTransaction.save(function(err) {
      if (err) {
        console.log(err);
        return res.json({success: false, msg: 'Error saving transaction'});
      }
      res.json({success: true, msg: 'Successful created new transaction.'});
    });
  });
});

apiRoutes.get('/purchases', function(req,res){
  Transaction.find({ buyer_id: req.user._id},{}, function(err, transactions){
    if(err) return res.send(500, err.message);
    res.status(200).json(transactions);
  });
});

apiRoutes.get('/sales', function(req,res){
  Transaction.find({ seller_id: req.user._id},{}, function(err, transactions){
    if(err) return res.send(500, err.message);
    res.status(200).json(transactions);
  });
});

// route to show a random message (GET http://localhost:8080/api/)
apiRoutes.get('/', function(req, res) {
  console.log("User" + req.user);
  res.json({ message: 'Welcome to the coolest API on earth!' });
});

// route to return all users (GET http://localhost:8080/api/users)
apiRoutes.get('/users', function(req, res) {
  User.find({}, function(err, users) {
    res.json(users);
  });
});   

// apply the routes to our application with the prefix /api
app.use('/api', apiRoutes);

app.listen(3000, function() {
    console.log("Node server running on http://localhost:3000");
  });

