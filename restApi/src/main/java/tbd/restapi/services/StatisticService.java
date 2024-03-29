package tbd.restapi.services;

import org.springframework.beans.factory.annotation.Autowired;

import java.awt.Stroke;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.*;
import tbd.restapi.models.Genre;
import tbd.restapi.models.Statistic;
import tbd.restapi.models.Artist;
import tbd.restapi.repositories.ArtistRepository;
import tbd.restapi.repositories.GenreRepository;
import tbd.restapi.repositories.StatisticRepository;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import javax.validation.constraints.Null;

@RestController
@RequestMapping(value = "/statistics")

public class StatisticService {
    @Autowired
    private StatisticRepository statisticRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ArtistRepository artistRepository;
    
    @CrossOrigin
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Statistic> getAllStatistics(){
        return this.statisticRepository.findAll();
    }
    @CrossOrigin
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Statistic getUser(@PathVariable int id)
    {
        return statisticRepository.findStatisticById(id);
    }

    @CrossOrigin
    @RequestMapping(value = "name/{name}", method = RequestMethod.GET)
    @ResponseBody
    public Statistic getUser(@PathVariable String name)
    {
        List<Statistic> staAux = statisticRepository.findStatisticByName(name);

        return staAux.get(staAux.size()-1);
    }
    
    @CrossOrigin
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> createStatistic(@RequestBody Statistic statistic){
        Map<String,Object> response = new HashMap<>();
        if(this.artistRepository.findArtistByName(statistic.getName())== null){
            response.put("Error", "No se encontro el artista");
            return response;
        }
        response.put("Status", "Se ha agregado la estadistica");
        statistic.setArtist(this.artistRepository.findArtistByName(statistic.getName()));
        statistic.setGenre(this.artistRepository.findArtistByName(statistic.getName()).getGenre());
        this.artistRepository.findArtistByName(statistic.getName()).getStatistic().add(statistic);
        this.statisticRepository.save(statistic);
        return response;

    }

   


    @CrossOrigin
    @RequestMapping(value = "/best10/popularArtist", method = RequestMethod.GET)
    @ResponseBody
    public List<Statistic> obtenerArtistasMasPopulares()
    {
        List<Artist> allArtists= artistRepository.findAll();
        List<Statistic> response = new ArrayList<Statistic>();
        List<Statistic> statisticsAux = new ArrayList<Statistic>();
        List<Float> totalTweetsList = new ArrayList<Float>();
        for(Artist artista : allArtists){
            String name = artista.getName();
            float totalTweets = 0;
            List<Statistic> artistStatistic = this.statisticRepository.findStatisticsByNameOrderByDateDesc(name);
            if(artistStatistic.size()>0){
                totalTweets = artistStatistic.get(0).getTotal_tweets();
                statisticsAux.add(artistStatistic.get(0));
                totalTweetsList.add(totalTweets);
            }
        }
        int aux = 0;
        for(int i = 0; i<totalTweetsList.size(); i++){
            float maximo = 0;
            int index = 0;
            Statistic estadistica = new Statistic();
            for(int j = 0; j<totalTweetsList.size(); j++){
                if(totalTweetsList.get(j)>maximo){
                    maximo = totalTweetsList.get(j);
                    index = j;
                    estadistica = statisticsAux.get(j);
                }
            }
            totalTweetsList.remove(index);
            statisticsAux.remove(index);
            response.add(estadistica);
            aux = aux + 1 ;
            if(aux == 10){
                break;
            }
        }
        
       
        return response;

    }

    @CrossOrigin
    @RequestMapping(value = "/best10/artistIncrease", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<HashMap> obtenerArtistaMayorCrecimiento()
    {
        List<HashMap> response = new ArrayList<HashMap>();
        HashMap<String, Object> responseAux = new LinkedHashMap<String, Object>();
        List<Artist> allArtists= artistRepository.findAll();
        List<List<Statistic>> statisticsAux = new ArrayList<List<Statistic>>();
        List<Float> crecimientoList = new ArrayList<Float>();
        for(Artist artista : allArtists){
            String name = artista.getName();
            float crecimiento = 0;
            List<Statistic> artistStatistic = this.statisticRepository.findStatisticsByNameOrderByDateDesc(name);
            if(artistStatistic.size()>5){
                crecimiento = artistStatistic.get(0).getTotal_tweets() - artistStatistic.get(5).getTotal_tweets();
                crecimientoList.add(crecimiento);
                statisticsAux.add(artistStatistic);
            }
            
            
        }
        List<Integer> listaIndices = new ArrayList<Integer>();
        int aux = 0;
        for(int i = 0; i < crecimientoList.size(); i++){
            float maximo = 0;
            int index = 0;
            for(int j = 0; j < crecimientoList.size(); j++){
                if(crecimientoList.get(j)>maximo){
                    index = j;
                    maximo = crecimientoList.get(j);
                }
            }
            listaIndices.add(index);
            crecimientoList.remove(index);        
            aux = aux+1;
            if(aux == 10){
                break;
            }
        }
        for(int j = 5; j >= 0; j--){
            responseAux.put("fecha", statisticsAux.get(0).get(j).getDate());
            for(int i = 0; i < listaIndices.size(); i++){
                responseAux.put(statisticsAux.get(i).get(j).getName(),statisticsAux.get(i).get(j).getTotal_tweets());
            }
            response.add(responseAux);
            responseAux = new LinkedHashMap<String, Object>();  

        }
        
        return response;

    }

    @CrossOrigin
    @RequestMapping(value = "/worst10/genres", method = RequestMethod.GET)
    @ResponseBody
    public List<Statistic> obtenerDiezPeoresGenerosTotales()
    {
        List<Artist> allArtists= artistRepository.findAll();
        List<Statistic> response = new ArrayList<Statistic>();
        List<Statistic> statisticsAux = new ArrayList<Statistic>();
        List<Float> divitions = new ArrayList<Float>();


        
        for(Artist element : allArtists) {
            String name = element.getName();
            List<Statistic> artistStatistic = this.statisticRepository.findStatisticsByNameOrderByDateDesc(name);
            if(artistStatistic.size()>0){
                float totalTweetsNegativos = artistStatistic.get(0).getNegativeTweets();
                //float totalTweetsNegativos = artistStatistic.get(0).getNegativeTweets();
                
                divitions.add(totalTweetsNegativos);
                statisticsAux.add(artistStatistic.get(0));
            
            }
            
        
        }
        
        int aux = 0;
        List<Float> divisionesFinal = new ArrayList<Float>();
        for(int i = 0; i<divitions.size(); i++){
            float maximo = 0;
            int index = 0;
            Statistic estadistica = new Statistic();
            for(int j = 0; j<divitions.size(); j++){
                if(divitions.get(j)>maximo){
                    maximo = divitions.get(j);
                    index = j;
                    estadistica = statisticsAux.get(j);
                }
            }
            divitions.remove(index);
            statisticsAux.remove(index);
            int flag = 0;
            for(Statistic elemento : response){
                if(artistRepository.findFirstArtistByName(elemento.getName()).getGenre().getName().equals(artistRepository.findFirstArtistByName(estadistica.getName()).getGenre().getName())){
                    aux = aux -1;
                    flag = 1;
                    break;
                }
            }
            if(flag == 0){
                response.add(estadistica);
                divisionesFinal.add(maximo);
            }
            aux = aux + 1 ;
            if(aux == 10){
                break;
            }
        }
       
        for(Statistic elemento : response){
            elemento.setName(artistRepository.findFirstArtistByName(elemento.getName()).getGenre().getName());
        }
        
       
        return response;

    }
    @CrossOrigin
    @RequestMapping(value = "/best10/genres", method = RequestMethod.GET)
    @ResponseBody
    public List<Statistic> obtenerDiezMejoresGenerosTotales()
    {
        List<Artist> allArtists= artistRepository.findAll();
        List<Statistic> response = new ArrayList<Statistic>();
        List<Statistic> statisticsAux = new ArrayList<Statistic>();
        List<Float> divitions = new ArrayList<Float>();


        
        for(Artist element : allArtists) {
            String name = element.getName();
            List<Statistic> artistStatistic = this.statisticRepository.findStatisticsByNameOrderByDateDesc(name);
            if(artistStatistic.size()>0){
                float totalTweetsPositivos = artistStatistic.get(0).getPositiveTweets();
                //float totalTweetsNegativos = artistStatistic.get(0).getNegativeTweets();
                
                divitions.add(totalTweetsPositivos);
                statisticsAux.add(artistStatistic.get(0));
            
            }
            
        
        }
        
        int aux = 0;
        System.out.println(divitions.size());
        System.out.println(statisticsAux.size());
        List<Float> divisionesFinal = new ArrayList<Float>();
        for(int i = 0; i<divitions.size(); i++){
            float maximo = 0;
            int index = 0;
            Statistic estadistica = new Statistic();
            for(int j = 0; j<divitions.size(); j++){
                if(divitions.get(j)>maximo){
                    maximo = divitions.get(j);
                    index = j;
                    estadistica = statisticsAux.get(j);
                }
            }
            divitions.remove(index);
            statisticsAux.remove(index);

            int flag = 0;
            for(Statistic elemento : response){
                if(artistRepository.findFirstArtistByName(elemento.getName()).getGenre().getName().equals(artistRepository.findFirstArtistByName(estadistica.getName()).getGenre().getName())){
                    aux = aux -1;
                    flag = 1;
                    break;
                }
            }
            if(flag == 0){
                response.add(estadistica);
                divisionesFinal.add(maximo);
            }
            aux = aux + 1 ;
            if(aux == 10){
                break;
            }
        }
        for(Statistic elemento : response){
            elemento.setName(artistRepository.findFirstArtistByName(elemento.getName()).getGenre().getName());
        }
        
       
        return response;

    }


    @CrossOrigin
    @RequestMapping(value = "/worst10/artistAllGenres", method = RequestMethod.GET)
    @ResponseBody
    public List<Statistic> obtenerDiezPeoresTotales()
    {   
        List<Artist> allArtists= artistRepository.findAll();
        List<Statistic> response = new ArrayList<Statistic>();
        List<Statistic> statisticsAux = new ArrayList<Statistic>();
        List<Float> divitions = new ArrayList<Float>();


        
        for(Artist element : allArtists) {
            String name = element.getName();
            List<Statistic> artistStatistic = this.statisticRepository.findStatisticsByNameOrderByDateDesc(name);
            if(artistStatistic.size()>0){
                float totalTweetsNegativos = artistStatistic.get(0).getNegativeTweets();
                //float totalTweetsNegativos = artistStatistic.get(0).getNegativeTweets();
                
                divitions.add(totalTweetsNegativos);
                statisticsAux.add(artistStatistic.get(0));
            
            }
            
        
        }
        
        int aux = 0;

        List<Float> divisionesFinal = new ArrayList<Float>();
        for(int i = 0; i<divitions.size(); i++){
            float maximo = 0;
            int index = 0;
            Statistic estadistica = new Statistic();
            for(int j = 0; j<divitions.size(); j++){
                if(divitions.get(j)>maximo){
                    maximo = divitions.get(j);
                    index = j;
                    estadistica = statisticsAux.get(j);
                }
            }
            divitions.remove(index);
            statisticsAux.remove(index);
            response.add(estadistica);
            divisionesFinal.add(maximo);
            aux = aux + 1 ;
            if(aux == 10){
                break;
            }
        }
        
        
       
        return response;

    }

    @CrossOrigin
    @RequestMapping(value = "/best10/artistAllGenres", method = RequestMethod.GET)
    @ResponseBody
    public List<Statistic> obtenerDiezMejoresTotales()
    {
        List<Artist> allArtists= artistRepository.findAll();
        List<Statistic> response = new ArrayList<Statistic>();
        List<Statistic> statisticsAux = new ArrayList<Statistic>();
        List<Float> divitions = new ArrayList<Float>();


        
        for(Artist element : allArtists) {
            String name = element.getName();
            List<Statistic> artistStatistic = this.statisticRepository.findStatisticsByNameOrderByDateDesc(name);
            if(artistStatistic.size()>0){
                float totalTweetsPositivos = artistStatistic.get(0).getPositiveTweets();
                //float totalTweetsNegativos = artistStatistic.get(0).getNegativeTweets();
                
                divitions.add(totalTweetsPositivos);
                statisticsAux.add(artistStatistic.get(0));
            
            }
            
        
        }

        
        
        int aux = 0;
        System.out.println(divitions.size());
        System.out.println(statisticsAux.size());
        List<Float> divisionesFinal = new ArrayList<Float>();
        for(int i = 0; i<divitions.size(); i++){
            float maximo = 0;
            int index = 0;
            Statistic estadistica = new Statistic();
            for(int j = 0; j<divitions.size(); j++){
                if(divitions.get(j)>maximo){
                    maximo = divitions.get(j);
                    index = j;
                    estadistica = statisticsAux.get(j);
                }
            }
            divitions.remove(index);
            statisticsAux.remove(index);
            
            response.add(estadistica);
            divisionesFinal.add(maximo);
            aux = aux + 1 ;
            if(aux == 10){
                break;
            }
        }
        
        
       
        return response;

    }

    @CrossOrigin
    @RequestMapping(value = "/best10/genre/{genre_name}", method = RequestMethod.GET)
    @ResponseBody
    public List<Statistic> obtenerDiezMejorValoradosPorGenero( @PathVariable("genre_name") String genre_name)
    {
        List<Artist> allArtists= artistRepository.findAll();
        List<Statistic> response = new ArrayList<Statistic>();
        List<Statistic> statisticsAux = new ArrayList<Statistic>();
        List<Float> positiveTweets = new ArrayList<Float>();
        for(Artist artista: allArtists){
            if(artista.getGenre().getName().equals(genre_name)){
                String name = artista.getName();
                
                List<Statistic> artistStatistic = this.statisticRepository.findStatisticsByNameOrderByDateDesc(name);
                if(artistStatistic.size()>0){

                    positiveTweets.add(artistStatistic.get(0).getPositiveTweets());
                    statisticsAux.add(artistStatistic.get(0));
                }
            }
        }
        int aux = 0;

        for(int i = 0; i<positiveTweets.size(); i++){
            float maximo = 0f;
            int index = 0;
            Statistic estadistica = new Statistic();
            for(int j = 0; j<positiveTweets.size(); j++){
                if(positiveTweets.get(j)>=maximo){
                    maximo = positiveTweets.get(j);
                    index = j;
                    estadistica = statisticsAux.get(j);
                }
            }
            positiveTweets.remove(index);
            statisticsAux.remove(index);
            response.add(estadistica);
            aux = aux + 1 ;
            if(aux == 10){
                break;
            }
        }
        return response;

    }
    @CrossOrigin
    @RequestMapping(value = "/worst10/genre/{genre_name}", method = RequestMethod.GET)
    @ResponseBody
    public List<Statistic> obtenerDiezPeorValoradosPorGenero( @PathVariable("genre_name") String genre_name)
    {
        List<Artist> allArtists= artistRepository.findAll();
        List<Statistic> response = new ArrayList<Statistic>();
        List<Statistic> statisticsAux = new ArrayList<Statistic>();
        List<Float> negativeTweets = new ArrayList<Float>();
        for(Artist artista: allArtists){
            if(artista.getGenre().getName().equals(genre_name)){
                String name = artista.getName();
                
                List<Statistic> artistStatistic = this.statisticRepository.findStatisticsByNameOrderByDateDesc(name);
                if(artistStatistic.size()>0){

                    negativeTweets.add(artistStatistic.get(0).getNegativeTweets());
                    statisticsAux.add(artistStatistic.get(0));
                }
            }
        }
        int aux = 0;

        for(int i = 0; i<negativeTweets.size(); i++){
            float maximo = 0f;
            int index = 0;
            Statistic estadistica = new Statistic();
            for(int j = 0; j<negativeTweets.size(); j++){
                if(negativeTweets.get(j)>=maximo){
                    maximo = negativeTweets.get(j);
                    index = j;
                    estadistica = statisticsAux.get(j);
                }
            }
            negativeTweets.remove(index);
            statisticsAux.remove(index);
            response.add(estadistica);
            aux = aux + 1 ;
            if(aux == 10){
                break;
            }
        }
        
        
       
        return response;

    }

    


}

