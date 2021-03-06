package com.psycaptr.rBNB.Services;

import com.google.api.Http;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.psycaptr.rBNB.Models.Contract;
import com.psycaptr.rBNB.Models.Property;
import com.psycaptr.rBNB.Models.Rating;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@DependsOn("FBInitialize")
public class PropertyService {
    Firestore db = FirestoreClient.getFirestore();

    public ResponseEntity<HttpStatus> addPropertyByUserId(Property property, String userId) throws ExecutionException, InterruptedException {
        property.setOwnerId(userId);
        String propertyId = addPropertyToDB(property);
        addPropertyIdToUser(userId, propertyId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    public String addPropertyToDB(Property property) {
        String propertyId = db.collection("Properties").document().getId();
        property.setId(propertyId);
        ApiFuture<WriteResult> collectionsApiFuture = db.collection("Properties").document(propertyId).set(property);
        return propertyId;
    }

    private void addPropertyIdToUser(String userId, String propertyId) {
        DocumentReference user = db.collection("Users").document(userId);
        user.update("propertiesId", FieldValue.arrayUnion(propertyId));
    }

    public ResponseEntity<String> deletePropertyById(String id) throws ExecutionException, InterruptedException {
        if(id.equals("") || id.equals(" ")) {
            return new ResponseEntity<>("Property was not deleted: Provided id is not acceptable.",HttpStatus.NOT_ACCEPTABLE);
        }
        ApiFuture<QuerySnapshot> future = db.collection("Properties").whereEqualTo("id",id).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if(documents.isEmpty()) {
            return new ResponseEntity<>("Property was not deleted: No property was found.",HttpStatus.NOT_FOUND);
        }
        if(documents.size()!=1) {
            return new ResponseEntity<>("Property was not deleted: At least two properties share the same id.",HttpStatus.CONFLICT);
        }
        ApiFuture<WriteResult> writeResult = db.collection("Properties").document(id).delete();
        return new ResponseEntity<>("Property successfully deleted.",HttpStatus.OK);
    }

    public List<Property> getAllProperties(String ownerId) throws ExecutionException, InterruptedException {
        List<Property> properties = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = db.collection("Properties").whereEqualTo("isListed",true).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            properties.add(document.toObject(Property.class));
        }
        Stream<Property> propertyStream = properties.stream()
                .filter(property -> !Objects.equals(property.getOwnerId(), ownerId));
        return propertyStream.collect(Collectors.toList());
    }

    public ResponseEntity<List<Property>> getPropertiesByUserId(String ownerId) throws ExecutionException, InterruptedException {
        if(ownerId.equals("")) {
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }

        List<Property> properties = new ArrayList<>();

        ApiFuture<QuerySnapshot> future = db.collection("Properties").whereEqualTo("ownerId",ownerId).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if(!documents.isEmpty()) {
            for (QueryDocumentSnapshot document : documents) {
                properties.add(document.toObject(Property.class));
            }
            return new ResponseEntity<>(properties,HttpStatus.OK);
        }
        return new ResponseEntity<>(null,HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<Integer> getPropertiesAmountByUserId(String ownerId) throws ExecutionException, InterruptedException {
        if(ownerId.equals("")) {
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
        ApiFuture<QuerySnapshot> future = db.collection("Properties").whereEqualTo("ownerId",ownerId).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        return ResponseEntity.ok(documents.size());
    }


    public ResponseEntity<HttpStatus> updateIsListed(String propertyId, boolean isListed) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = db.collection("Properties").document(propertyId);
        documentReference.update("isListed",isListed);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public static boolean isPropertyListed(String propertyId) throws ExecutionException, InterruptedException {
        DocumentSnapshot property = getPropertyByIdV2(propertyId);
        if(!property.exists()) {
            return false;
        }
        return Boolean.TRUE.equals(property.getBoolean("isListed"));
    }

    private static DocumentSnapshot getPropertyByIdV2(String propertyId) throws ExecutionException, InterruptedException {
        DocumentReference propertyReference = FirestoreClient.getFirestore().collection("Properties").document(propertyId);
        ApiFuture<DocumentSnapshot> propertyQuery = propertyReference.get();
        return propertyQuery.get();
    }

    public ResponseEntity<Property> getPropertyById(String propertyId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = db.collection("Properties").whereEqualTo("id",propertyId).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        if(documents.size()==0) {
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Property>(documents.get(0).toObject(Property.class),HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> updatePropertyById(String propertyId, Map<String, Object> newProperty) throws ExecutionException, InterruptedException {
        db.collection("Properties").document(propertyId).update(newProperty);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> updatePropertyRatingById(String propertyId, int rating) throws ExecutionException, InterruptedException {
        Rating oldRating = getPropertyById(propertyId).getBody().getRating();
        Rating newRating = calculateRatingValue(oldRating,rating);
        db.collection("Properties").document(propertyId).update("rating.value",newRating.getValue());
        db.collection("Properties").document(propertyId).update("rating.amount",newRating.getAmount());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Rating calculateRatingValue(Rating oldRating, int rating) {
        double newValue = (oldRating.getValue() * oldRating.getAmount() + rating*20)/(oldRating.getAmount()+1);
        return new Rating(newValue,oldRating.getAmount()+1);
    }


}
