package com.upgrad.movieapp.services;

import com.upgrad.movieapp.dao.MovieDao;
import com.upgrad.movieapp.dto.TheatreDTO;
import com.upgrad.movieapp.dto.UserDTO;
import com.upgrad.movieapp.entities.Movie;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class MovieServiceImpl implements MovieService{


  @Autowired
  private MovieDao movieDao ;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${userAPI}")
  private String userAPI;

  @Value("${theatreAPI}")
  private String theatreAPI;


  @Override
  public Movie acceptMovieDetails(Movie movie) {
    return movieDao.save(movie);
  }

  @Override
  public List<Movie> acceptMultipleMovieDetails(List<Movie> movies) {
    List<Movie> savedMovies = new ArrayList<>();
    for(Movie movie : movies){
      savedMovies.add(acceptMovieDetails(movie));
    }
    return savedMovies;
  }

  @Override
  public Movie getMovieDetails(int id) {
    return movieDao.findById(id).get();
  }

  @Override
  public Movie updateMovieDetails(int id, Movie movie) {
    Movie savedMovie = getMovieDetails(id);
    savedMovie.setDuration(movie.getDuration());
    savedMovie.setTrailerUrl(movie.getTrailerUrl());
    savedMovie.setCoverPhotoUrl(movie.getCoverPhotoUrl());
    savedMovie.setReleaseDate(movie.getReleaseDate());
    savedMovie.setMovieName(movie.getMovieName());
    savedMovie.setMovieDescription(movie.getMovieDescription());

    movieDao.save(savedMovie);

    return savedMovie;
  }

  @Override
  public boolean deleteMovie(int id) {
    Movie savedMovie = getMovieDetails(id);

    if(savedMovie == null){
      return false ;
    }

    movieDao.delete(savedMovie);
    return true;
  }

  @Override
  public List<Movie> getAllMovies() {
    return movieDao.findAll();
  }

  @Override
  public Page<Movie> getPaginatedMovieDeatails(Pageable pageable) {
    return movieDao.findAll(pageable);
  }

  @Override
  public Boolean bookMovie(UserDTO user, Movie movie, TheatreDTO theatre) {

    //Check whether requested movie is a valid movie.
    Optional<Movie> requestedMovie = movieDao.findById(movie.getMovieId());
    if(!requestedMovie.isPresent())
      return false;

    //Check whether User is valid
    Map<String, String> userUriMap = new HashMap<>();
    userUriMap.put("id",String.valueOf(user.getUserId()));
    String userAppUrl = userAPI + "/{id}";
    UserDTO receivedUser = (restTemplate.getForObject(userAppUrl,UserDTO.class,userUriMap));
    if(receivedUser==null){
      return false;
    }

    //Check whether theatre and movie combination is valid
    Map<String, String> theatreUriMap = new HashMap<>();
    theatreUriMap.put("theatreId",String.valueOf(theatre.getTheatreId()));
    theatreUriMap.put("movieId",String.valueOf(theatre.getMovieId()));
    String theatreAppUrl = theatreAPI + "/{theatreId}/movie/{movieId}";
    TheatreDTO receivedTheatre = (restTemplate.getForObject(theatreAppUrl,TheatreDTO.class,theatreUriMap));
    if(receivedTheatre==null){
      return false;
    }

    return true;
  }
}
